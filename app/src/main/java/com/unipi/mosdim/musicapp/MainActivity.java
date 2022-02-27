package com.unipi.mosdim.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    Button buttonLogout;
    ImageButton search_button;
    Button homeButton, profileButton, settingsButton;
    FirebaseAuth mAuth;
    LinearLayout layout;
    ScrollView scrollView;
    MediaPlayer mediaPlayer = new MediaPlayer();
    TextView movingText,minText,maxText;
    Button playButton, nextButton, previousButton;
    EditText search;
    ArrayList<String> songNames = new ArrayList<>();
    ArrayList<String> artistNames = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    ArrayList<String> links = new ArrayList<>();
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Button> buttons = new ArrayList<>();
    String getLink="";
    int length=0,i;

    private Handler mSeekbarUpdateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeButton = findViewById(R.id.homebutton);
        profileButton = findViewById(R.id.profilebutton);
        settingsButton = findViewById(R.id.settingsbutton);
        search = findViewById(R.id.search_genreEditText);
        search_button = findViewById(R.id.imageButton);

        homeButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }

        });

        profileButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),UserProfile.class);
                startActivity(i);
            }

        });

        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction()== KeyEvent.ACTION_DOWN) && (i == keyEvent.KEYCODE_ENTER)){
                    search_button.performClick();

                    //close keyboard
                    View current_view = MainActivity.this.getCurrentFocus();
                    if (current_view != null) {
                        InputMethodManager manager =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(current_view.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if((search.length() > 2 && i1<i2) || search.getText().toString().isEmpty())
                    search_button.performClick();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBar3);
        minText = findViewById(R.id.minTime);
        maxText = findViewById(R.id.maxTime);
        movingText = findViewById(R.id.movingText);
        movingText.setSelected(true);
        buttonLogout = findViewById(R.id.buttonLogout);
        mAuth = FirebaseAuth.getInstance();
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);

        buttonLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            mediaPlayer.reset();
            finish();
        });
        DatabaseReference myRef;
        myRef = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("songs");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    songNames.add((String) snapshot.child("name").getValue());   //τιτλος βιβλιων
                    artistNames.add((String) snapshot.child("artist").getValue());
                    links.add((String) snapshot.child("link").getValue());
                    locations.add((String) snapshot.child("location").getValue());
                    categories.add((String) snapshot.child("category").getValue());

                    getAllData(i);
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

//        DatabaseReference myRef2 = FirebaseDatabase.getInstance("https://musicapp-ad62e-default-rtdb.firebaseio.com/").getReference().child("user_pref");
//        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    if (!((String)snapshot.child("genre").getValue()).equals("rock")){
//                        Toast.makeText(MainActivity.this, "Rock", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(this, 50);
            setTime();
        }
    };


    public void setTime(){
        String time =(new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getCurrentPosition()));
        minText.setText(time);
        if(minText.getText().equals(maxText.getText())){
            int y=0;
            for (String element : links) {
                if (element == getLink) {
                    break;
                }
                y++;
            }
            if(!links.contains(getLink)) {
                playMusic(0);
            }
            if((links.size()-1)>y) {
                playMusic(y + 1);
            }
            else if(links.size()-1<=y){
                getLink="";
                playMusic(0);
            }
        }
    }

    public void getAllData(int i){
        //hardcoded components
        layout = findViewById(R.id.layout_parent);
        scrollView = findViewById(R.id.scrollView_parent);

        LinearLayout.LayoutParams lparams_inside = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        TextView t1 = new TextView(this);
        t1.setText(artistNames.get(i));

        LinearLayout l1h = new LinearLayout(this);
        l1h.setWeightSum(1);    //controls the weights in l1v and playbtn
        l1h.setOrientation(LinearLayout.HORIZONTAL);
        l1h.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout l1v = new LinearLayout(this);
        l1v.setOrientation(LinearLayout.VERTICAL);
        lparams_inside.weight = 9;
        l1v.setLayoutParams(lparams_inside);

        TextView title1 = new TextView(this);
        title1.setText(songNames.get(i));
        l1v.addView(title1);
        l1v.addView(t1);

        buttons.add(i,new Button(this));
        lparams_inside.weight = 1;
        buttons.get(i).setLayoutParams(lparams_inside);
        buttons.get(i).setBackgroundResource(R.drawable.ic_small_play);
        buttons.get(i).setEnabled(true);
        buttons.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusic(i);
            }
        });


        l1h.addView(l1v);
        l1h.addView(buttons.get(i));

        this.layout.addView(l1h);
    }

    public void playMusic(int i){
        if(!getLink.equals(links.get(i))) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(links.get(i));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        for(Button btn1 : buttons)
                            btn1.setBackgroundResource(R.drawable.ic_small_play);
                        buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
                        playButton.setBackgroundResource(R.drawable.pauseimg);
                        getLink = links.get(i);
                        maxText.setText((new SimpleDateFormat("m:ss")).format(new Date(mediaPlayer.getDuration())));
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1));
                        movingText.setText(songNames.get(i)+","+ artistNames.get(i));
                    }
                });
                mediaPlayer.prepare();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if(mediaPlayer.isPlaying()){
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_play);
            playButton.setBackgroundResource(R.drawable.playimg);
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }
        else{
            buttons.get(i).setBackgroundResource(R.drawable.ic_small_pause);
            playButton.setBackgroundResource(R.drawable.pauseimg);
            mediaPlayer.start();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar,0);
        }
    }

    public void playClick(View view){
        int y=0;
        for (String element : links) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if(!links.contains(getLink))
            y=0;
        playMusic(y);
    }

    public void previousButton(View view) {
        int y = 0;
        for (String element : links) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if (!links.contains(getLink))
            y = 0;
        if (y > 0)
            playMusic(y - 1);
        else {
            getLink="";
            playMusic(0);
        }
    }
    public void nextButton(View view){
        int y=0;
        for (String element : links) {
            if (element == getLink) {
                break;
            }
            y++;
        }
        if(!links.contains(getLink)) {
            playMusic(0);
        }
        if((links.size()-1)>y)
            playMusic(y+1);
        else if(links.size()-1<=y){
            getLink="";
            playMusic(0);
        }
    }

    public void searchGenre(View view){
        int i=0;
        layout.removeAllViews();
        if (!((EditText)findViewById(R.id.search_genreEditText)).getText().toString().isEmpty()){
            //int found = 0;
            for (String song_genre: categories) {
                if (song_genre.equals(((EditText)findViewById(R.id.search_genreEditText)).getText().toString())){
                    //found++;
                    getAllData(i);
                }
                i++;
            }
            //Toast.makeText(this, found + " song(s) found", Toast.LENGTH_SHORT).show();
        }
        else{
            for (int j = 0; j < songNames.size(); j++) {
                getAllData(j);
            }
            //Toast.makeText(this, songName.size() + " song(s) found", Toast.LENGTH_SHORT).show();
        }

    }


    protected void onStart () {
        super.onStart();
        String user = mAuth.getUid();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else {
            mAuth.getCurrentUser();
        }
    }
}