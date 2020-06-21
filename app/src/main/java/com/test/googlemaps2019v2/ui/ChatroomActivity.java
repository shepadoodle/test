package com.test.googlemaps2019v2.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.test.googlemaps2019v2.R;
import com.test.googlemaps2019v2.UserClient;
import com.test.googlemaps2019v2.adapters.ChatMessageRecyclerAdapter;
import com.test.googlemaps2019v2.models.ChatMessage;
import com.test.googlemaps2019v2.models.Chatroom;
import com.test.googlemaps2019v2.models.Event;
import com.test.googlemaps2019v2.models.EventLocation;
import com.test.googlemaps2019v2.models.User;
import com.test.googlemaps2019v2.models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;



public class ChatroomActivity extends AppCompatActivity implements
        View.OnClickListener
{

    private static final String TAG = "ChatroomActivity";

    //widgets
    private Chatroom mChatroom;
    private EditText mMessage;
    private ImageView mAvatarImage;


    //vars
    private ListenerRegistration mChatMessageEventListener, mEventListListener;
    private RecyclerView mChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private FirebaseFirestore mDb;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<Event> mEventList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<EventLocation> mEventLocations = new ArrayList<>();
//    private ArrayList<Integer> mImageResources1 = new ArrayList<>();
    private ArrayList<Integer> mImageResources2 = new ArrayList<>();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        mMessage = findViewById(R.id.input_message);
        mChatMessageRecyclerView = findViewById(R.id.chatmessage_recycler_view);


        findViewById(R.id.checkmark).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        getIncomingIntent();
        initChatroomRecyclerView();
        getChatroomUsers();
        getChatroomEvents();
        getChatroomEventsLocation();
//        getImageResouces1();
        getImageResouces2();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView_Bar);
        Menu navMenu = bottomNavigationView.getMenu();
        MenuItem menuItem = navMenu.getItem(1);
        menuItem.setTitle(mChatroom.getTitle());
        menuItem.setChecked(false);

        SpannableString spanString = new SpannableString(menuItem.getTitle().toString());
        int end = spanString.length();
        spanString.setSpan(new RelativeSizeSpan(2.0f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        menuItem.setTitle(spanString);


        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()){
                            case android.R.id.home:{
                                MapFragment fragment =
                                        (MapFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                                if(fragment != null){
                                    if(fragment.isVisible()){
                                        getSupportFragmentManager().popBackStack();
                                        return true;
                                    }
                                }
                                finish();
                                return true;
                            }
                            case R.id.action_chatroom_map_fragment:{
                                inflateMapFragment();
                                return true;
                            }
                            case R.id.action_chatroom_leave:{
                                leaveChatroom();
                                return true;
                            }
                        }
                        return false;
                    }
                });

    }

    private void getUserLocation(User user){
        DocumentReference locationsRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null){
                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });
    }

    private void getChatMessages(){

        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }
                        if(queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);
                                if(!mMessageIds.contains(message.getMessage_id())){
                                    mMessageIds.add(message.getMessage_id());
                                    mMessages.add(message);
                                    mChatMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                }
                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void getChatroomUsers(){

        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list));

        mEventListListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "(getChatroomUsers)onEvent: Listen failed.", e);
                            return;
                        }
                        if(queryDocumentSnapshots != null){
                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                mUserList.add(user);
                                getUserLocation(user);
                            }
                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });
    }

    private void getChatroomEvents(){

        CollectionReference eventsRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_event));

        mEventListListener = eventsRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "(getChatroomEvents) onEvent: Listen failed.", e);
                            return;
                        }
                        if(queryDocumentSnapshots != null){
                            // Clear the list and add all the users again
                            mEventList.clear();
                            mEventList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Event event = doc.toObject(Event.class);
                                mEventList.add(event);
                            }
                            Log.d(TAG, "onEvent: user list size: " + mEventList.size());
                        }
                    }
                });
    }

    private void getChatroomEventsLocation(){

        CollectionReference eventsRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_event_locations));

        mEventListListener = eventsRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "(getChatroomEvents) onEvent: Listen failed.", e);
                            return;
                        }
                        if(queryDocumentSnapshots != null){
                            // Clear the list and add all the events again
                            mEventLocations.clear();
                            mEventLocations = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                EventLocation eventLocation = doc.toObject(EventLocation.class);
                                mEventLocations.add(eventLocation);
                            }
                            Log.d(TAG, "onEvent: user list size: " + mEventList.size());
                        }
                    }
                });
    }

    private void initChatroomRecyclerView(){

        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(mMessages, new ArrayList<User>(),this);
        mChatMessageRecyclerView.setAdapter(mChatMessageRecyclerAdapter);
        mChatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mChatMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mChatMessageRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMessages.size() > 0){
                                mChatMessageRecyclerView.smoothScrollToPosition(
                                        mChatMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });
    }


    private void insertNewMessage(){
        String message = mMessage.getText().toString();

        if(!message.equals("")){
            message = message.replaceAll(System.getProperty("line.separator"), "");

            DocumentReference newMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            ChatMessage newChatMessage = new ChatMessage();
            newChatMessage.setMessage(message);
            newChatMessage.setMessage_id(newMessageDoc.getId());

            User user = ((UserClient)(getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            newChatMessage.setUser(user);

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        clearMessage();
                    }else{
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, R.string.somethingWentWrong, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearMessage(){
        mMessage.setText("");
    }

    private void inflateMapFragment(){

        MapFragment fragment = MapFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
        bundle.putParcelableArrayList(getString(R.string.intent_event_list), mEventList);
        bundle.putParcelableArrayList(getString(R.string.intent_event_locations), mEventLocations);
        bundle.putString("chat_id",mChatroom.getChatroom_id());

        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

	private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra(getString(R.string.intent_chatroom))){
            mChatroom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            joinChatroom();
        }
    }

    private void leaveChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        joinChatroomRef.delete();
        Intent intent = new Intent(ChatroomActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void joinChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        User user = ((UserClient)(getApplicationContext())).getUser();
        joinChatroomRef.set(user); // Don't care about listening for completion.
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChatMessageEventListener != null){
            mChatMessageEventListener.remove();
        }
        if(mEventListListener != null){
            mEventListListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                MapFragment fragment =
                        (MapFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                if(fragment != null){
                    if(fragment.isVisible()){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }
            case R.id.action_chatroom_map_fragment:{
                inflateMapFragment();
                return true;
            }
            case R.id.action_chatroom_leave:{
                leaveChatroom();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.checkmark:{
                insertNewMessage();
            }
        }
    }

//    private void getImageResouces1(){
//        mImageResources1.add(R.drawable.avatar_cwm_logo);
//        mImageResources1.add(R.drawable.avatar_cartman_cop);
//        mImageResources1.add(R.drawable.avatar_eric_cartman);
//        mImageResources1.add(R.drawable.avatar_ike);
//        mImageResources1.add(R.drawable.avatar_kyle);
//        mImageResources1.add(R.drawable.avatar_satan);
//        mImageResources1.add(R.drawable.avatar_chef);
//        mImageResources1.add(R.drawable.avatar_tweek);
//    }

    private void getImageResouces2(){
        mImageResources2.add(R.drawable.avatar_batman);
        mImageResources2.add(R.drawable.avatar_borodach);
        mImageResources2.add(R.drawable.avatar_boy);
        mImageResources2.add(R.drawable.avatar_farmer);
        mImageResources2.add(R.drawable.avatar_man);
        mImageResources2.add(R.drawable.avatar_old_man);
        mImageResources2.add(R.drawable.avatar_student);
        mImageResources2.add(R.drawable.avatar_woman1);
        mImageResources2.add(R.drawable.avatar_woman2);
        mImageResources2.add(R.drawable.avatar_woman3);
        mImageResources2.add(R.drawable.avatar_woman4);
        mImageResources2.add(R.drawable.avatar_woman5);
    }
}
