package achivementtrackerbyamit.example.achivetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import achivementtrackerbyamit.example.achivetracker.active.ActiveGoalFragment;
import achivementtrackerbyamit.example.achivetracker.active.GoingCLass;
import achivementtrackerbyamit.example.achivetracker.archive.ArchiveGoalFragment;
import achivementtrackerbyamit.example.achivetracker.rank.RankFragment;
import achivementtrackerbyamit.example.achivetracker.rank.Topper;

public class HomeActivity extends AppCompatActivity {



    ChipNavigationBar chipNavigationBar;
    String currentUserID;
    DatabaseReference RootRef,NewRef;
    ImageView profile_button;

    public static int confirmation = 0;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        InitializeMethods();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag_container_nav,
                        new ActiveGoalFragment()).commit();
        bottomMenu();

        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ProfileIntent = new Intent ( HomeActivity.this,ProfileActivity.class );
                startActivity ( ProfileIntent );

            }
        });

        RetriveUserImage();

    }

    private void InitializeMethods() {

        chipNavigationBar = findViewById(R.id.bottom_nav_bar);
        chipNavigationBar.setItemSelected(R.id.nav_home,
                true);
        profile_button = findViewById(R.id.logout_btn);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid ();
        RootRef= FirebaseDatabase.getInstance ().getReference ().child("Users").child(currentUserID).child("Goals").child("Active");
        NewRef = FirebaseDatabase.getInstance().getReference().child("Topper").child(currentUserID);
        FirebaseDatabase.getInstance ().getReference ().child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Getting information of the user
                DataSnapshot nameSnapshot=snapshot.child("name");
                DataSnapshot imageSnapshot=snapshot.child("user_image");
                DataSnapshot goalsSnapshot=snapshot.child("Goals").child("Active");

                int max =-1;
                String GoalName = "No goals yet";

                // Iterating over goals to find the one with highest consistency
                for (DataSnapshot ds : goalsSnapshot.getChildren()){
                    try {
                        Map<String,Object> map = (Map<String, Object>) ds.getValue();
                        Object goal_name = map.get("GoalName");
                        Object consis = map.get("Consistency");
                        int maxConsis = Integer.parseInt(String.valueOf(consis));
                        if (maxConsis>max) {
                            max = maxConsis;
                            GoalName = (String) goal_name;
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                // Setting the best goal in the topper node
                Topper topper = new Topper(GoalName,String.valueOf(max),imageSnapshot.getValue(String.class),nameSnapshot.getValue(String.class));
                NewRef.setValue(topper);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void RetriveUserImage() {
        // Getting profile picture to set in the profile button
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object pfpUrl = snapshot.child("user_image").getValue();
                if(pfpUrl != null)
                {
                    Picasso.get().load(pfpUrl.toString()).placeholder(R.drawable.profile).error(R.drawable.profile).into(profile_button);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onBackPressed(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(HomeActivity.this,R.style.AlertDialogTheme);
        builder.setTitle("Confirm Exit");
        builder.setIcon(R.drawable.main_kogo);
        builder.setMessage("Do you really want to exit?");
        builder.setBackground(getResources().getDrawable(R.drawable.material_dialog_box , null));
        builder.setCancelable(false);
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(HomeActivity.this, "Exit cancelled", Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }

    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener
                (new ChipNavigationBar.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(int i) {
                        Fragment fragment = null;
                        switch (i){
                            case R.id.nav_home:
                                fragment = new ActiveGoalFragment();
                                break;
                            case R.id.nav_new_archive:
                                fragment = new ArchiveGoalFragment();
                                break;
                            case R.id.nav_new_ranking:
                                fragment = new RankFragment();
                                break;
                            case R.id.nav_settings:
                                fragment = new SettingsFragment();
                                break;
                        }
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frag_container_nav,
                                        fragment).commit();

                    }
                });
    }
}