package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.CounterView.createCount
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.AuthViewModel


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun showVideo(post: Post)
    fun goToPost(post: Post)
    fun syncPost()
    fun syncOnePost(post: Post)

    fun goToPhoto(id: Long)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {


    fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
        referencedIds.forEach { id ->
            rootView.findViewById<View>(id).setOnClickListener(listener)
        }
    }

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
         //   like.isChecked = post.likedByMe
            like.text = createCount(post.likes)
            share.text = createCount(post.shares)

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            Glide.with(avatar)
                .load("http://192.168.1.10:9999/avatars/${post.authorAvatar}")
                .placeholder(R.drawable.baseline_emoji_emotions_24)
                .error(R.drawable.remove_red_eye_24)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            photoImage.load("http://192.168.1.10:9999/media/${post.attachment?.url}")

            if (post.videoUrl != null) {
                groupVideo.visibility = View.VISIBLE
            } else {
                groupVideo.visibility = View.GONE
            }

            if (post.unSaved) {
                unSavedPost.visibility = View.VISIBLE
            } else {
                unSavedPost.visibility = View.GONE
            }

            unSavedPost.setOnClickListener {
                onInteractionListener.syncOnePost(post)
            }

//            if(post.attachment!=null) {
//                Glide.with (attachmentImage)
//                    .load("http://192.168.1.10:9999/images/${post.attachment.url}")
//                    .placeholder(R.drawable.baseline_autorenew_24)
//                    .error(R.drawable.ic_cancel_48)
//                    .timeout(10_000)
//                    .into(attachmentImage)
//                attachmentImage.visibility = View.VISIBLE
//            } else {
//                attachmentImage.visibility = View.GONE
//            }

            videoView.setOnClickListener {
                onInteractionListener.showVideo(post)
            }
            like.setOnClickListener {
                like.isChecked = !like.isChecked
                if (!post.unSaved) {
                    onInteractionListener.like(post)
                }
            }
            share.setOnClickListener {
                if (!post.unSaved) {
                    onInteractionListener.share(post)
                }
            }
            groupPost.setAllOnClickListener {
                Log.d("MyLog", "groupPost ${post.id}")
                onInteractionListener.goToPost(post)
            }

            if (post.attachment != null) {
                photoImage.visibility = View.VISIBLE
            } else {
                photoImage.visibility = View.GONE
            }

            photoImage.setOnClickListener {
                onInteractionListener.goToPhoto(post.id)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            watchCount.text = post.watches.toString()
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}