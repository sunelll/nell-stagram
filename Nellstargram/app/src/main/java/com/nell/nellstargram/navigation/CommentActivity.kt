package com.nell.nellstargram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nell.nellstargram.R
import com.nell.nellstargram.navigation.model.AlarmDTO
import com.nell.nellstargram.navigation.model.ContentDTO
import com.nell.nellstargram.navigation.utill.FcmPush
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {

    var contentUid : String? = null
    var destinationUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        comment_recyclerview.adapter = CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)


        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_msg.text.toString()
            comment.timestamp = System.currentTimeMillis().toString()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)
            commentAlram(destinationUid!!, comment_edit_msg.text.toString())
            comment_edit_msg.setText("")


        }

    }

//    fun commentDelete(destinationUid : String) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        FirebaseFirestore.getInstance().collection("alarms").document().set(ContentDTO::class.java)
//    }


    fun commentAlram(destinationUid : String, message: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind = 1
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var msg = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_comment) + "리플: " + message
        FcmPush.instance.sendMessage(destinationUid, "넬스타그램에서 알람이 도착했어요", msg)

    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                    .collection("images")
                    .document(contentUid!!)
                    .collection("comments")
                    .orderBy("timestamp")
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        comments.clear()
                        if(querySnapshot == null) return@addSnapshotListener

                        for(snapshot in querySnapshot.documents!!){
                            comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView
            view.commentview_textview_comment.text = comments[p1].comment
            view.commentview_textview_profile.text = comments[p1].userId

            FirebaseFirestore.getInstance()
                    .collection("profileImages")
                    .document(comments[p1].uid!!)
                    .get()
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            var url = task.result!!["image"]
                            Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentview_imageview_profile)
                        }
                    }
        }

        override fun getItemCount(): Int {
           return comments.size
        }


    }
}