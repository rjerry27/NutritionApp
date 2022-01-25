package com.example.mynutrition;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static final float END_SCALE = 0.7f;
    ImageView menuIcon;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    LinearLayout contentView;
    ImageView addBtn;
    TextView nameView;


    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    TextView graphMsg;

    String fruitData;
    String vegetableData;
    String grainsData;
    String proteinData;
    String dairyData;

    PieChart pieChart;
    ArrayList<PieEntry> yData;
    ArrayList <String> xData;
    ArrayList<Integer> colors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);
        addBtn = findViewById(R.id.add_icon);
        graphMsg = findViewById(R.id.testview);
        pieChart = findViewById(R.id.pieChart);
        nameView = findViewById(R.id.nameView);


        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                nameView.setText("Hey "+dataSnapshot.child(firebaseUser.getUid()).child("name").getValue(String.class)+",");
                graphMsg.setText("here's your calorie consumption:");

                if (firebaseUser != null) {

                    fruitData = dataSnapshot.child(firebaseUser.getUid()).child("fruit").getValue(String.class);
                    vegetableData = dataSnapshot.child(firebaseUser.getUid()).child("vegetable").getValue(String.class);
                    grainsData = dataSnapshot.child(firebaseUser.getUid()).child("grains").getValue(String.class);
                    dairyData = dataSnapshot.child(firebaseUser.getUid()).child("dairy").getValue(String.class);
                    proteinData = dataSnapshot.child(firebaseUser.getUid()).child("protein").getValue(String.class);


                    yData = new ArrayList<>();
                    xData = new ArrayList<>();
                    colors = new ArrayList<>();


                    if(fruitData!=null) {
                        yData.add(new PieEntry(Float.parseFloat(fruitData)));
                        xData.add("fruits");
                        colors.add(Color.BLUE);
                    }
                    if(vegetableData!=null) {
                        yData.add(new PieEntry(Float.parseFloat(vegetableData)));
                        xData.add("vegetables");
                        colors.add(Color.RED);
                    }
                    if(grainsData!=null) {
                        yData.add(new PieEntry(Float.parseFloat(grainsData)));
                        xData.add("grains");
                        colors.add(Color.GREEN);
                    }
                    if(dairyData!=null) {
                        yData.add(new PieEntry(Float.parseFloat(dairyData)));
                        xData.add("dairy");
                        colors.add(Color.YELLOW);
                    }
                    if(proteinData!=null) {
                        yData.add(new PieEntry(Float.parseFloat(proteinData)));
                        xData.add("protein");
                        colors.add(Color.MAGENTA);
                    }

                    PieDataSet pieDataSet = new PieDataSet(yData,"Calories");
                    pieDataSet.setSliceSpace(2);
                    pieDataSet.setValueTextSize(12);
                    pieDataSet.setColors(colors);

                    Legend legend = pieChart.getLegend();
                    legend.setForm(Legend.LegendForm.CIRCLE);

                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.getLegend().setEnabled(false);
                    pieChart.invalidate();

                    pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            int pos1 = e.toString().indexOf("y: ");
                            String calories = e.toString().substring((pos1+3));
                            int pos2;
                            String calories2 = "";

                            for (int i = 0; i < yData.size(); i++){
                                pos2 = yData.get(i).toString().indexOf("y: ");
                                calories2 = yData.get(i).toString().substring((pos2+3));

                                if (calories.equals(calories2)){
                                    pos1 = i;
                                    break;
                                }
                            }
                            String foodGroup = xData.get(pos1);
                            Toast.makeText(Dashboard.this,calories+" calories consumed of "+foodGroup,Toast.LENGTH_SHORT).show();


                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });


                    //testView.setText(fruitData+vegetableData+grainsData+dairyData+proteinData);



                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        navigationDrawer();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this,Additem.class));
            }
        });

    }

    private void navigationDrawer() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        animateNavigationDrawer();

    }

    private void animateNavigationDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset*(1-END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth()*slideOffset;
                final float xOffsetDiff = contentView.getWidth()*diffScaledOffset/2;
                final float xTranslation = xOffset-xOffsetDiff;
                contentView.setTranslationX(xTranslation);

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(),Dashboard.class));
                break;
            case R.id.nav_add:
                startActivity(new Intent(getApplicationContext(),Additem.class));
                break;
            case R.id.nav_log:
                startActivity(new Intent(getApplicationContext(),imagesActivity.class));
                break;
                case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(),Settings.class));
                break;
        }

        return true;
    }
}
