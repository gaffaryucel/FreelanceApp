package com.androiddevelopers.freelanceapp.view.discover

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import com.androiddevelopers.freelanceapp.R
import com.androiddevelopers.freelanceapp.adapters.discover.TagAdapter
import com.androiddevelopers.freelanceapp.adapters.discover.ViewPagerAdapterForCreateDiscover
import com.androiddevelopers.freelanceapp.databinding.CustomDialogChooseImageSourceBinding
import com.androiddevelopers.freelanceapp.databinding.FragmentDiscoverCreatePostBinding
import com.androiddevelopers.freelanceapp.model.DiscoverPostModel
import com.androiddevelopers.freelanceapp.util.Status
import com.androiddevelopers.freelanceapp.util.checkPermissionImageCamera
import com.androiddevelopers.freelanceapp.util.checkPermissionImageGallery
import com.androiddevelopers.freelanceapp.util.compressJpegInBackground
import com.androiddevelopers.freelanceapp.util.convertUriToBitmap
import com.androiddevelopers.freelanceapp.util.createImageUri
import com.androiddevelopers.freelanceapp.util.hideBottomNavigation
import com.androiddevelopers.freelanceapp.util.showBottomNavigation
import com.androiddevelopers.freelanceapp.util.toast
import com.androiddevelopers.freelanceapp.viewmodel.discover.DiscoverCreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DiscoverCreatePostFragment : Fragment() {
    private var _binding: FragmentDiscoverCreatePostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscoverCreatePostViewModel by viewModels()
    private lateinit var dialogChooseImageSource: Dialog
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>
    private val selectedBitmapImages = mutableListOf<Bitmap>()
    private val selectedByteArrayImages = mutableListOf<ByteArray>()
    private lateinit var imageUri: Uri
    private val viewPagerAdapter = ViewPagerAdapterForCreateDiscover()
    private val tagAdapter = TagAdapter()
    private var tags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = createImageUri(requireContext())
        setupLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverCreatePostBinding.inflate(inflater, container, false)

        //ilk açılışta create ekranı olduğu için delete butonunu gizliyoruz
        binding.deleteButtonDiscoverCreate.visibility = View.GONE

        dialogChooseImageSource = createDialogChooseImageSource()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setProgressBar = false
        setupOnClicks()
        observeLiveData(viewLifecycleOwner)

        with(binding) {
            rvTagAdapter = tagAdapter

            viewPagerDiscoverCreate.adapter = viewPagerAdapter
            indicatorDiscoverCreate.setViewPager(viewPagerDiscoverCreate)
        }

    }

    private fun setupLaunchers() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                val bitmap = convertUriToBitmap(imageUri, requireActivity())
                selectedBitmapImages.add(bitmap)
                compressJpegInBackground(bitmap) { byteArrayImage ->
                    selectedByteArrayImages.add(byteArrayImage)
                }
                viewModel.setBitmapImages(selectedBitmapImages.toList())
            }
        }

        imageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let {
                        val bitmap = convertUriToBitmap(it, requireActivity())
                        selectedBitmapImages.add(bitmap)
                        compressJpegInBackground(bitmap) { byteArrayImage ->
                            selectedByteArrayImages.add(byteArrayImage)
                        }
                        viewModel.setBitmapImages(selectedBitmapImages.toList())
                    }
                }
            }
    }

    private fun setupOnClicks() {
        with(binding) {
            fabChooseImageSource.setOnClickListener {
                dialogChooseImageSource.show()
            }

            tagAddTextInputLayout.setEndIconOnClickListener {
                tags.add(tagAddEditText.text.toString())
                viewModel.setTags(tags.toList())
                tagAddEditText.text = null
            }

            tagAdapter.clickListener = { list ->
                viewModel.setTags(list.toList())
            }

            viewPagerAdapter.listenerImages = { images ->
                viewModel.setBitmapImages(images.toList())
            }

            saveButtonDiscoverCreate.setOnClickListener {
                if (selectedByteArrayImages.isNotEmpty()) {
                    if (descriptionTextInputEditText.text.toString().isNotBlank()) {
                        if (tags.isNotEmpty()) {
                            viewModel.addImageAndDiscoverPostToFirebase(
                                selectedByteArrayImages, DiscoverPostModel(
                                    description = descriptionTextInputEditText.text.toString(),
                                    tags = tags
                                )
                            )
                        } else {
                            "Lütfen etiket girin".toast(binding.root)
                        }
                    } else {
                        "Lütfen açıklama girin".toast(binding.root)
                    }
                } else {
                    "Lütfen resim ekleyin".toast(binding.root)
                }
            }
        }
    }

    private fun observeLiveData(owner: LifecycleOwner) {
        with(viewModel) {
            firebaseMessage.observe(owner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        "Upload Success".toast(binding.root)
                        Navigation.findNavController(binding.root)
                            .navigate(R.id.action_global_navigation_discover)
                    }

                    Status.ERROR -> {
                        "Upload Failed".toast(binding.root)
                    }

                    Status.LOADING -> {
                        it.data?.let { data -> binding.setProgressBar = data }
                    }
                }
            }

            liveDateBitmapImages.observe(owner) { images ->
                selectedBitmapImages.clear()
                selectedBitmapImages.addAll(images.toList())
                viewPagerAdapter.refreshList(images.toList())
                with(binding) {
                    //indicatoru viewpager yeni liste ile set ediyoruz
                    indicatorDiscoverCreate.setViewPager(viewPagerDiscoverCreate)
                }
            }

            imageSizeLiveData.observe(owner) {
                //seçilen resim olmadığında viewpager 'ı gizleyip boş bir resim gösteriyoruz
                //resim seçildiğinde işlemi tersine alıyoruz
                with(binding) {
                    if (it == 0 || it == null) {
                        imagePlaceHolderDiscoverCreate.visibility = View.VISIBLE
                        layoutImageViewsDiscoverCreate.visibility = View.INVISIBLE
                    } else {
                        imagePlaceHolderDiscoverCreate.visibility = View.INVISIBLE
                        layoutImageViewsDiscoverCreate.visibility = View.VISIBLE
                    }
                }
            }

            tagsLiveData.observe(owner) { list ->
                tagAdapter.tagsRefresh(list.toList())

                tags.clear()
                tags.addAll(list.toList())
            }
        }
    }


    private fun openImagePicker() {
        val imageIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageLauncher.launch(imageIntent)
    }

    private fun openCamera() {
        cameraLauncher.launch(imageUri)
    }

    private fun createDialogChooseImageSource(): Dialog {
        val view = CustomDialogChooseImageSourceBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.cardCameraImageSource.setOnClickListener {
            if (checkPermissionImageCamera(requireActivity(), 800)) {
                openCamera()
                dialog.dismiss()
            }
        }

        view.cardGalleryImageSource.setOnClickListener {
            if (checkPermissionImageGallery(requireActivity(), 801)) {
                openImagePicker()
                dialog.dismiss()
            }
        }

        view.imageCloseChooseImageSource.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        showBottomNavigation(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}