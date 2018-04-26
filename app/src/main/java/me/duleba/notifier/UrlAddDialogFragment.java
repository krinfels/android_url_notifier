package me.duleba.notifier;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;

import java.io.IOException;

import me.duleba.notifier.database.HashedUrl;
import me.duleba.notifier.database.UrlDatabase;

public class UrlAddDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity)getActivity();
        final View view = activity.getLayoutInflater().inflate(R.layout.add_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setView(view)
                .setPositiveButton("Accept", (dialog, which) -> {
            String newUrl = ((TextView)view.findViewById(R.id.newUrl)).getText().toString();
            newUrl = URLUtil.guessUrl(newUrl);

           if(!Patterns.WEB_URL.matcher(newUrl).matches())
               //TODO Show Error Dialog or sth
               return;

           HashedUrl newEntry = new HashedUrl();
           newEntry.setUrl(newUrl);
           UrlDatabase.getDB(activity).urlDao().insert(newEntry);


           //Update hash for the new entry
           new Thread(() -> {
               long hash = 0;
               try {
                   hash = UrlHasher.hashUrl(newEntry.getUrl());
               } catch (IOException e) {
                   UrlDatabase.getDB(activity)
                           .urlDao().setInvalid(newEntry.getUrl());
                   return;
               }

               UrlDatabase.getDB(activity)
                       .urlDao().updateHash(newEntry.getUrl(), hash);
           }).start();

        })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        Dialog result = builder.create();
        result.setCanceledOnTouchOutside(false);

        return result;
    }
}
