package me.duleba.notifier;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.duleba.notifier.database.HashedUrl;
import me.duleba.notifier.database.UrlDatabase;

import static java.lang.Math.min;

public class UrlViewAdapter extends RecyclerView.Adapter<UrlViewAdapter.ViewHolder>{
    private List<HashedUrl> dataSource;
    private MainActivity activity;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView urlName;
        public TextView lastChanged;

        public ViewHolder(View view) {
            super(view);
            urlName = itemView.findViewById(R.id.urlName);
            lastChanged = itemView.findViewById(R.id.lastChanged);
        }
    }

    public UrlViewAdapter(MainActivity activity) {
        this.dataSource = UrlDatabase.getDB(activity).urlDao().getAll();
        this.activity = activity;

        Observer<List<HashedUrl>> observer = hashedUrls -> {
            int i;
            for(i=0; i < min(dataSource.size(), hashedUrls.size()); i++) {
                if(!dataSource.get(i).equals(hashedUrls.get(i))) {
                    dataSource.set(i, hashedUrls.get(i));
                    UrlViewAdapter.this.notifyItemChanged(i);
                }
            }

            if(dataSource.size() < hashedUrls.size()) {
                int oldSize = dataSource.size();
                hashedUrls.listIterator(dataSource.size()).forEachRemaining(dataSource::add);
                UrlViewAdapter.this.notifyItemRangeInserted(oldSize, dataSource.size() - oldSize);
            }else if(dataSource.size() > hashedUrls.size()) {
                int oldSize = dataSource.size();

                while(dataSource.size() > hashedUrls.size())
                    dataSource.remove(dataSource.size()-1);

                UrlViewAdapter.this.notifyItemRangeRemoved(dataSource.size(), oldSize - dataSource.size());
            }
        };
        UrlDatabase.getDB(activity).urlDao().getObservable().observeForever(observer);

    }

    @NonNull
    @Override
    public UrlViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_url, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UrlViewAdapter.ViewHolder holder, int position) {
        HashedUrl entry = dataSource.get(position);
        long lastChanged = entry.getLastChanged();

        holder.urlName.setText(entry.getUrl().toString());
        if(lastChanged < 0) {
            if(lastChanged == -1)
                holder.lastChanged.setText(R.string.downloadPending);
            else if(lastChanged == -2)
                holder.lastChanged.setText(R.string.downloadFailed);
        }else
            holder.lastChanged.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(lastChanged * 1000)));

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(UrlViewAdapter.this.activity)
                    .setMessage(activity.getString(R.string.deleteDialogMessage) + " " + dataSource.get(position).getUrl())
                    .setPositiveButton(R.string.deleteDialogPositive, (dialog, which) -> UrlDatabase.getDB(activity).urlDao().delete(dataSource.get(position)))
                    .setNegativeButton(R.string.deleteDialogNegative, ((dialog, which) -> dialog.cancel()))
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }


}
