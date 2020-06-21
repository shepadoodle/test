package com.test.googlemaps2019v2.ui;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.test.googlemaps2019v2.R;
import com.test.googlemaps2019v2.adapters.ImageListRecyclerAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserImageListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserImageListFragment extends Fragment implements ImageListRecyclerAdapter.ImageListRecyclerClickListener{

    private static final String TAG = "UserImageListFragment";
    private static final int NUM_COLUMNS = 2;

    //widgets
    private RecyclerView mRecyclerView;


    //vars
    private ArrayList<Integer> mImageResources1 = new ArrayList<>();
    private ArrayList<Integer> mImageResources2 = new ArrayList<>();
    private IProfile mIProfile;

    public UserImageListFragment() {
        // Required empty public constructor
    }


    public static UserImageListFragment newInstance() {
        return new UserImageListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_image_list, container, false);
        mRecyclerView = view.findViewById(R.id.image_list_recyclerview);

//        getImageResouces1();
        getImageResouces2();
        initRecyclerview();

        return view;
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
    private void getImageResouces2() {
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
    private void initRecyclerview(){
        ImageListRecyclerAdapter mAdapter = new ImageListRecyclerAdapter(getActivity(), mImageResources2, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIProfile = (IProfile) context; //getActivity()
    }

    @Override
    public void onImageSelected(int position) {
        mIProfile.onImageSelected(mImageResources2.get(position));
    }
}








