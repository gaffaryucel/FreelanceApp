package com.androiddevelopers.freelanceapp.view.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.androiddevelopers.freelanceapp.R
import com.androiddevelopers.freelanceapp.databinding.FragmentFreelancerInfoBinding
import com.androiddevelopers.freelanceapp.model.Education
import com.androiddevelopers.freelanceapp.model.PortfolioItem
import com.androiddevelopers.freelanceapp.util.PermissionUtils
import com.androiddevelopers.freelanceapp.util.UserStatus
import com.androiddevelopers.freelanceapp.viewmodel.profile.BaseProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FreelancerInfoFragment : Fragment() {

    private lateinit var viewModel: BaseProfileViewModel

    private var _binding: FragmentFreelancerInfoBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_IMAGE_CAPTURE = 101
    private val REQUEST_IMAGE_PICK = 102
    private var selectedImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[BaseProfileViewModel::class.java]
        _binding = FragmentFreelancerInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSignup.setOnClickListener {
            makeFreelancer()
        }
        binding.ivUserProfilePhoto.setOnClickListener{
            openCamera()
        }
    }

    private fun makeFreelancer() {
        val fullName = binding.editFullName.text.toString()
        val bio = binding.editBio.text.toString()
        val phoneNumber = binding.editPhone.text.toString()
        val jobTitle = binding.editJobTitle.text.toString()
        val jobDescription = binding.etJobDescription.text.toString()
        val skills = binding.editSkills.text.toString()
        val portfolio = binding.editPortfolio.text.toString()
        val education = binding.editEducation.text.toString()
        val termsChecked = binding.checkboxTerms.isChecked
        val privacyChecked = binding.checkboxPrivacy.isChecked



        // Check if any field is empty
        if (fullName.isEmpty() || bio.isEmpty() || phoneNumber.isEmpty() || jobTitle.isEmpty() ||
            skills.isEmpty() || portfolio.isEmpty() || jobDescription.isEmpty() ||
            education.isEmpty() || !termsChecked || !privacyChecked || selectedImage == null) {
            // Show error message or handle accordingly
            Toast.makeText(requireContext(), "Please fill in all fields and agree to terms and privacy policy", Toast.LENGTH_SHORT).show()
            return
        }else{
            viewModel.updateUserInfo("fullName", fullName)
            viewModel.updateUserInfo("bio", bio)
            viewModel.updateUserInfo("phone", phoneNumber)
            viewModel.updateUserInfo("jobTitle", jobTitle)
            viewModel.updateUserInfo("jobDescription", jobDescription)
            viewModel.updateUserInfo("skills", listOf(skills))
            viewModel.updateUserInfo("portfolio", listOf(PortfolioItem(portfolio,portfolio,portfolio)))
            viewModel.updateUserInfo("education", listOf(Education(education,education,2024)))
            viewModel.updateUserInfo("userType", UserStatus.FREELANCER)
            viewModel.saveImageToStorage(selectedImage!!)
        }
        // All fields are filled, proceed with registration
        // Perform registration logic here

    }
    private fun openCamera() {
        if (PermissionUtils.requestCameraAndStoragePermissions(requireActivity())) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun openGallery() {
        if (PermissionUtils.requestCameraAndStoragePermissions(requireActivity())) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_PICK)
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.ivUserProfilePhoto.setImageBitmap(imageBitmap)
                    selectedImage = imageBitmap
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    binding.ivUserProfilePhoto.setImageURI(selectedImageUri)
                    if (selectedImageUri != null) {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            selectedImageUri
                        )
                        selectedImage = imageBitmap
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
    }

    override fun onPause() {
        super.onPause()
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE
    }
}
