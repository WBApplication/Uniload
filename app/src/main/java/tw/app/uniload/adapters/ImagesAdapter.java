package tw.app.uniload.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.utils.OnSingleClickListener;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.widget.ProgressBar;
import tw.app.uniload.R;
import tw.app.uniload.utils.Download;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private List<String> mData;
    public List<Boolean> isDownloaded = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;

    public ImagesAdapter(List<String> data, Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;

        for (int i = 0; i < mData.size(); i++) {
            isDownloaded.add(false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context).load(mData.get(position)).into(holder.imageView);

        if (mData.get(position).contains(".jpg") || mData.get(position).contains(".png")) {
            holder.imageTypeText.setText("Image");
        } else if (mData.get(position).contains(".mp4") || mData.get(position).contains(".webm")) {
            holder.imageTypeText.setText("Video");
        }

        if (isDownloaded.get(position) == true) {
            holder.downloadImageButton.setText("Success!");
            holder.downloadImageButton.setIcon(null);
            holder.downloadImageButton.setBackgroundTintList(ColorStateList.valueOf(0xFF3CAB26));
            holder.downloadImageButton.setVisibility(View.VISIBLE);
            holder.downloadingProgressBar.setVisibility(View.GONE);
        }

        holder.imagePositionText.setText((position + 1) + " / " + mData.size());
        holder.downloadImageButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!isDownloaded.get(position)) {
                    holder.downloadImageButton.setBackgroundTintList(null);
                    holder.downloadImageButton.setVisibility(View.GONE);
                    holder.downloadingProgressBar.setVisibility(View.VISIBLE);

                    Download download = new Download(context, mData.get(position), new Download.DownloaderListener() {
                        @Override
                        public void OnProgress(int progress) {
                            holder.downloadingProgressBar.setProgress(progress);
                        }

                        @Override
                        public void OnSuccess() {
                            isDownloaded.set(position, true);
                            holder.downloadImageButton.setText("Success!");
                            holder.downloadImageButton.setIcon(null);
                            holder.downloadImageButton.setBackgroundTintList(ColorStateList.valueOf(0xFF3CAB26));
                            holder.downloadImageButton.setVisibility(View.VISIBLE);
                            holder.downloadingProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void OnError(String reason) {
                            holder.downloadingProgressBar.setVisibility(View.GONE);
                            holder.errorText.setText(reason);
                            holder.errorText.setVisibility(View.VISIBLE);
                            holder.retryDownloadImageButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        holder.retryDownloadImageButton.setOnClickListener(view -> {
            holder.downloadImageButton.setVisibility(View.VISIBLE);
            holder.downloadingProgressBar.setVisibility(View.GONE);
            holder.errorText.setVisibility(View.GONE);
            holder.retryDownloadImageButton.setVisibility(View.GONE);
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        MaterialButton downloadImageButton, retryDownloadImageButton;
        ProgressBar downloadingProgressBar;
        MaterialTextView imagePositionText, errorText, imageTypeText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            downloadImageButton = itemView.findViewById(R.id.download_image_button);
            downloadingProgressBar = itemView.findViewById(R.id.downloading_progress_bar);
            imagePositionText = itemView.findViewById(R.id.image_position_text);
            errorText = itemView.findViewById(R.id.error_text);
            retryDownloadImageButton = itemView.findViewById(R.id.retry_download_image_button);
            imageTypeText = itemView.findViewById(R.id.image_type_text);
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }
}
