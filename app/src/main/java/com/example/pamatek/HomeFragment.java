package com.example.pamatek;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.media.MediaPlayer;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    private VideoView videoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        videoView = view.findViewById(R.id.videoView);

        // Set video URI from raw resource
        Uri videoUri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.turoarial);
        videoView.setVideoURI(videoUri);

        // Start video automatically
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true); // Loop the video
            videoView.start();
        });

        return view;
    }
}
