package com.androiddevelopers.freelanceapp.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.androiddevelopers.freelanceapp.R
import com.androiddevelopers.freelanceapp.databinding.FragmentEditUserProfileInfoBinding
import com.androiddevelopers.freelanceapp.model.UserModel
import com.androiddevelopers.freelanceapp.viewmodel.EditUserProfileInfoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditUserProfileInfoFragment : Fragment() {

    private lateinit var viewModel: EditUserProfileInfoViewModel
    private val userData = MutableLiveData<UserModel>()

    private var _binding: FragmentEditUserProfileInfoBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[EditUserProfileInfoViewModel::class.java]
        _binding = FragmentEditUserProfileInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewProfile.setOnClickListener{
            val action = EditUserProfileInfoFragmentDirections.actionEditUserProfileInfoFragmentToEditMainProfileInfoFragment(
                userData.value?.username.toString(),
                userData.value?.bio.toString(),
                userData.value?.profileImageUrl.toString()
            )
            Navigation.findNavController(it).navigate(action)
        }
        binding.cardViewPersonalInfo.setOnClickListener{
            val action = EditUserProfileInfoFragmentDirections.actionEditUserProfileInfoFragmentToEditProfilePersonalInfoFragment(
                userData.value?.phone.toString(),
                userData.value?.fullName.toString(),
                userData.value?.location?.country.toString(),
                userData.value?.location?.city.toString(),
                userData.value?.location?.address.toString()
            )
            Navigation.findNavController(it).navigate(action)
        }
        observeLiveData()
    }
    private fun observeLiveData() {
        viewModel.userData.observe(viewLifecycleOwner, Observer { data ->
            userData.value = data
            binding.apply {
                user = data
            }
        })
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