package com.androiddevelopers.freelanceapp.repo

import android.graphics.Bitmap
import android.net.Uri
import com.androiddevelopers.freelanceapp.model.ChatModel
import com.androiddevelopers.freelanceapp.model.DiscoverPostModel
import com.androiddevelopers.freelanceapp.model.FollowModel
import com.androiddevelopers.freelanceapp.model.MessageModel
import com.androiddevelopers.freelanceapp.model.PreChatModel
import com.androiddevelopers.freelanceapp.model.UserModel
import com.androiddevelopers.freelanceapp.model.jobpost.EmployerJobPost
import com.androiddevelopers.freelanceapp.model.jobpost.FreelancerJobPost
import com.androiddevelopers.freelanceapp.model.notification.InAppNotificationModel
import com.androiddevelopers.freelanceapp.model.notification.PushNotification
import com.androiddevelopers.freelanceapp.util.NotificationType
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import okhttp3.ResponseBody
import retrofit2.Response

interface FirebaseRepoInterFace {
    // Auth işlemleri
    fun login(email: String, password: String): Task<AuthResult>
    fun forgotPassword(email: String): Task<Void>
    fun register(email: String, password: String): Task<AuthResult>

    // Firestore User işlemleri
    fun addUserToFirestore(data: UserModel): Task<Void>
    fun deleteUserFromFirestore(documentId: String): Task<Void>
    fun getUserDataByDocumentId(documentId: String): Task<DocumentSnapshot>
    fun getUsersFromFirestore(): Task<QuerySnapshot>
    fun getUsersFromFirestore(list: List<String>): Task<QuerySnapshot>
    fun uploadDataInUserNode(userId: String, data: Any, type: String, dataId: String): Task<Void>

    // Firestore Freelancer Job Post işlemleri
    fun addFreelancerJobPostToFirestore(post: FreelancerJobPost): Task<Void>
    fun getAllFreelancerJobPostFromFirestore(): Task<QuerySnapshot>
    fun getFreelancerJobPostWithDocumentByIdFromFirestore(documentId: String): Task<DocumentSnapshot>
    fun updateViewCountFreelancerJobPostWithDocumentById(
        postId: String,
        newCount: List<String>
    ): Task<Void>

    fun deleteFreelancerJobPostFromFirestore(postId: String): Task<Void>
    fun updateLikeFreelancerJobPostFromFirestore(
        postId: String,
        likes: List<String>
    ): Task<Void>

    fun updateSavedUsersFreelancerJobPostFromFirestore(
        postId: String,
        savedUsers: List<String>
    ): Task<Void>

    // Firestore Employer Job Post işlemleri
    fun addEmployerPostToFirestore(job: EmployerJobPost): Task<Void>
    fun getAllEmployerJobPostFromFirestore(): Task<QuerySnapshot>
    fun getEmployerJobPostWithDocumentByIdFromFirestore(documentId: String): Task<DocumentSnapshot>
    fun updateViewCountEmployerJobPostWithDocumentById(
        postId: String,
        newCount: List<String>
    ): Task<Void>

    fun deleteEmployerJobPostFromFirestore(postId: String): Task<Void>
    fun updateSavedUsersEmployerJobPostFromFirestore(
        postId: String,
        savedUsers: List<String>
    ): Task<Void>

    // Firestore Discover Post işlemleri
    fun uploadDiscoverPostToFirestore(post: DiscoverPostModel): Task<Void>
    fun getAllDiscoverPostsFromFirestore(): Task<QuerySnapshot>
    fun likePost(postId: String, updateData: HashMap<String, Any?>): Task<Void>
    fun getDiscoverPostDataFromFirebase(postId: String): Task<DocumentSnapshot>
    fun commentToDiscoverPost(postId: String, updateData: HashMap<String, Any?>): Task<Void>

    // Realtime Database Chat işlemleri
    fun sendMessageToRealtimeDatabase(
        userId: String,
        chatId: String,
        message: MessageModel
    ): Task<Void>

    fun addMessageInChatMatesRoom(
        chatMateId: String,
        chatId: String,
        message: MessageModel
    ): Task<Void>

    fun getAllMessagesFromRealtimeDatabase(currentUserId: String, chatId: String): DatabaseReference
    fun getAllFollowingUsers(currentUserId: String): DatabaseReference
    fun createChatRoomForOwner(currentUserId: String, chat: ChatModel): Task<Void>
    fun createChatRoomForChatMate(userId: String, chat: ChatModel): Task<Void>
    fun getAllChatRooms(currentUserId: String): DatabaseReference
    fun changeLastMessage(
        userId: String,
        chatId: String,
        message: String,
        time: String
    ): Task<Void>

    fun changeLastMessageInChatMatesRoom(
        chatMateId: String,
        chatId: String,
        message: String,
        time: String
    ): Task<Void>

    fun seeMessage(
        userId: String,
        chatId: String
    ): Task<Void>

    fun changeReceiverSeenStatus(
        receiverId: String,
        chatId: String
    ): Task<Void>

    //PreChatRoom
    fun getAllPreChatRooms(currentUserId: String): DatabaseReference
    fun createPreChatRoom(receiver: String, sender: String, chat: PreChatModel): Task<Void>

    //PreMessaging
    fun getAllMessagesFromPreChatRoom(currentUserId: String, chatId: String): DatabaseReference

    fun sendMessageToPreChatRoom(
        userId: String,
        receiver: String,
        chatId: String,
        message: MessageModel
    ): Task<Void>

    fun changeLastPreMessage(
        userId: String,
        receiver: String,
        chatId: String,
        message: String,
        time: String
    ): Task<Void>

    fun seePreMessage(
        userId: String,
        chatId: String
    ): Task<Void>

    fun changeReceiverPreSeenStatus(
        receiverId: String,
        chatId: String
    ): Task<Void>

    // Firebase Storage işlemleri

    fun addFreelancerPostImage(
        image: ByteArray,
        uId: String,
        postId: String,
    ): UploadTask

    fun addEmployerPostImage(
        uri: Uri,
        uId: String,
        postId: String,
    ): UploadTask

    fun addDiscoverPostImage(
        image: ByteArray,
        uId: String,
        postId: String
    ): UploadTask

    //User Profile Data İşlemleri
    fun getAllDiscoverPostsFromUser(userId: String, limit: Long): Task<QuerySnapshot>
    fun getAllEmployerJobPostsFromUser(userId: String, limit: Long): Task<QuerySnapshot>
    fun getAllFreelancerJobPostsFromUser(userId: String, limit: Long): Task<QuerySnapshot>
    fun follow(followerModel: FollowModel, followingModel: FollowModel): Task<Void>
    fun unFollow(currentUserId: String, followingId: String): Task<Void>
    fun updateUserData(userId: String, updateData: HashMap<String, Any?>): Task<Void>
    fun getFollowers(userId: String): DatabaseReference
    suspend fun uploadPhotoToStorage(bitmap: Bitmap, uid: String, imagePath: String): String?

    //Notification
    //Set
    suspend fun postNotification(notification: PushNotification): Response<ResponseBody>
    fun saveNotification(notification: InAppNotificationModel): Task<Void>

    //Get
    fun getNotificationsByType(
        userId: String,
        type: NotificationType,
        limit: Long
    ): Task<QuerySnapshot>

    fun getAllNotifications(userId: String, limit: Long): Task<QuerySnapshot>

    fun changeOnlineStatus(userId: String, onlineData: Boolean): Task<Void>

}


