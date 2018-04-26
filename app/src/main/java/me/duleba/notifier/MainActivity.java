package me.duleba.notifier;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.duleba.notifier.database.UrlDatabase;

public class MainActivity extends AppCompatActivity {
    private RecyclerView viewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load db from file
        new Thread(() -> UrlDatabase.getDB(this)).start();
        setContentView(R.layout.activity_url_list);

        JobInfo.Builder builder = new JobInfo.Builder(1,
                new ComponentName(this, UrlHasherService.class));
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        builder.setPeriodic(60 * 1000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresBatteryNotLow(true);
        scheduler.schedule(builder.build());

        viewList = findViewById(R.id.urlListView);
        viewList.setAdapter(new UrlViewAdapter(this));
        viewList.setLayoutManager(new LinearLayoutManager(this));


        FloatingActionButton addButton = findViewById(R.id.url_add);
        addButton.setOnClickListener(view -> new UrlAddDialogFragment().show(getFragmentManager(), "Add url"));
    }

    @Override
    protected void onDestroy() {
        //UrlDatabase.closeDB();
        super.onDestroy();
    }
}
