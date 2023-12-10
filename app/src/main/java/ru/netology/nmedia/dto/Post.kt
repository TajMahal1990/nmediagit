package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar:String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val shares: Int = 0,
    val watches: Int = 0,
    val videoUrl:String? = null,
    var unSaved:Boolean = true,
    var hidden:Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
)
data class Attachment(
    val url: String,
    val type: AttachmentType,
)

//data class Attachment(
//    val url:String,
//    val description:String,
//    val type:String
//)

