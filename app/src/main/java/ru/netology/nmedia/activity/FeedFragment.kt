package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.CurrentPostFragment.Companion.textArgument
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private  val authViewModel:AuthViewModel by activityViewModels()

    lateinit var binding:FragmentFeedBinding

//    private val viewModel: PostViewModel by viewModels(
//        ownerProducer = ::requireParentFragment
//    )

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun like(post: Post) {
                if(authViewModel.authenticated) {
                    viewModel.likeById(post)
                } else {
                    mustSignIn()
             //       post =post.copy(likedByMe = !post.likedByMe)
                }

            }

            override fun share(post: Post) {
                viewModel.share(post)

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun remove(post: Post) {
               // viewModel.removeById(post.id)
                viewModel.removeById(post)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)

                findNavController()
                    .navigate(R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            textArg = post.content
                        })
            }

            override fun showVideo(post: Post) {
                val intentVideo = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoUrl))
                startActivity(intentVideo)
            }

            override fun goToPost(post: Post) {

                findNavController()
                    .navigate(R.id.action_feedFragment_to_currentPostFragment,
                        Bundle().apply {
                            textArgument = post.id.toString()
                        })
            }

            override fun syncPost() {

            }

            override fun syncOnePost(post: Post) {
               // viewModel.syncPost()
                viewModel.syncOnePost(post)
            }

            override fun goToPhoto(id: Long) {
                findNavController()
                    .navigate(R.id.action_feedFragment_to_currentPhotoFragment,
                        Bundle().apply {
                            textArgument = id.toString()
                        })
            }
        })


        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRW.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val newPost = state.posts.size > adapter.currentList.size
            adapter.submitList(state.posts) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
            binding.emptyText.isVisible = state.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->

            if(state!=0) {
                  binding.newPost.setIconTintResource(R.color.red)
            //    binding.newPost.setBackgroundColor(Color.parseColor("#FF0000"))
                binding.newPost.isEnabled = true
                binding.buttonNewPosts.visibility = View.VISIBLE
            //    binding.newPost.setTextColor(R.color.red)
                binding.newPost.text = state.toString()
            } else {
                binding.newPost.setIconTintResource(R.color.purple_700)
            //    binding.newPost.setBackgroundColor(resources.getColor(R.color.purple_700, null))
                binding.newPost.isEnabled = false
                binding.buttonNewPosts.visibility = View.GONE
                binding.newPost.text = ""
            }
            println(state)
        }

        binding.newPost.setOnClickListener {
//            binding.newPost.text = ""
//            binding.buttonNewPosts.visibility = View.GONE
//            binding.newPost.isEnabled = false
//            viewModel.changeHidden()
            showNewPost()
        }

        binding.buttonNewPosts.setOnClickListener {
            showNewPost()
        }



        binding.swipeRW.setOnRefreshListener {
            viewModel.refreshPosts()
        }
//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            adapter.submitList(state.posts)
//            binding.progress.isVisible = state.loading
//            binding.errorGroup.isVisible = state.error
//            binding.emptyText.isVisible = state.empty
//        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            if(authViewModel.authenticated) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                mustSignIn()
            }

        }

//        binding.swipeRW.setOnRefreshListener {
//            viewModel.data.value?.refreshing = true
//            viewModel.loadPosts()
//            binding.swipeRW.isRefreshing = false
//        }

//        binding.list.adapter = adapter
//
//        viewModel.data.observe(viewLifecycleOwner) { posts ->
//            val newPost = posts.size > adapter.currentList.size
//            adapter.submitList(posts) {
//                if (newPost) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        }
        return binding.root
    }

    fun showNewPost() {
        binding.newPost.setIconTintResource(R.color.purple_700)
        binding.newPost.text = ""
        binding.buttonNewPosts.visibility = View.GONE
        binding.newPost.isEnabled = false
        viewModel.changeHidden()
    }

    fun mustSignIn() {
        val menuDialog = SignInOutDialogFragment("Нужна регистрация","Для этого действия необходимо войти в систему", R.drawable.info_24, "Sign In", "Позже")
        val manager = childFragmentManager
        menuDialog.show(manager, "Sign in")
    }
}

