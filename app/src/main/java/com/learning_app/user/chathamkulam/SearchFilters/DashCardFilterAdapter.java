package com.learning_app.user.chathamkulam.SearchFilters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.learning_app.user.chathamkulam.Fragments.ModuleList;
import com.learning_app.user.chathamkulam.Model.BackgroundWork.AlarmReceiver;
import com.learning_app.user.chathamkulam.Model.DashboardModel.DashEntityObjects;
import com.learning_app.user.chathamkulam.Model.MyBounceInterpolator;
import com.learning_app.user.chathamkulam.R;
import com.learning_app.user.chathamkulam.Sqlite.StoreEntireDetails;
import com.learning_app.user.chathamkulam.Viewer.NSPDFViewer;
import com.learning_app.user.chathamkulam.Viewer.QBPDFViewer;
import com.tonyodev.fetch.Fetch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
import static com.learning_app.user.chathamkulam.FetchDownloadManager.unzip;
import static com.learning_app.user.chathamkulam.Model.BackgroundWork.AlarmReceiver.deleteRecursive;

/**
 * Created by User on 5/14/2017.
 */

public class DashCardFilterAdapter extends RecyclerView.Adapter<DashCardFilterAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private List<DashEntityObjects> myList;
    private List<DashEntityObjects> mFilteredList;
    private String currentSubName;

    private StoreEntireDetails storeEntireDetails;

    public DashCardFilterAdapter(Context mContext, List<DashEntityObjects> myList) {
        super();

//        Getting all store data
        this.mContext = mContext;
        this.myList = myList;
        mFilteredList = myList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_dash_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final DashEntityObjects dashEntityObjects = mFilteredList.get(position);

        String course = dashEntityObjects.getCourse();
        String semester = dashEntityObjects.getSemester();
        final String subject = dashEntityObjects.getSubject_name();

        holder.txtTrail.setText(dashEntityObjects.getTrial());
        holder.txtPrice.setText(dashEntityObjects.getSubject_cost());
        holder.txtValid.setText("Valid till : " + dashEntityObjects.getValidityTill());

        Thread fetchThread = new Thread() {
            public void run() {

                String status = dashEntityObjects.getStatus();
                String progress = dashEntityObjects.getProgress();

                if (!status.equals("onCompleted") && !progress.equals("100")) {

                    String downloadId = dashEntityObjects.getDownload_id().trim();
                    Fetch fetch = Fetch.newInstance(mContext);

                    if (fetch != null) {
                        int idStatus = fetch.get(Long.parseLong(downloadId)).getStatus();

                        if (idStatus != 0) {
                            if (idStatus == Fetch.STATUS_DONE) {

                                storeEntireDetails = StoreEntireDetails.getInstance(mContext);
                                boolean IsEntry = storeEntireDetails.updateDownloadData(subject, String.valueOf(fetch.get(Long.parseLong(downloadId)).getProgress()), "onCompleted");

                                if (IsEntry) {
                                    Log.d("##status", "Successfully Updated");

                                } else {
                                    Log.d("##status", "Update failed");
                                }
                            }

                            if (idStatus == Fetch.STATUS_PAUSED) {

                                storeEntireDetails = StoreEntireDetails.getInstance(mContext);
                                boolean IsEntry = storeEntireDetails.updateDownloadData(subject, String.valueOf(fetch.get(Long.parseLong(downloadId)).getProgress()), "onDownloadPaused");

                                if (IsEntry) {
                                    Log.d("##status", "Successfully Updated");

                                } else {
                                    Log.d("##status", "Update failed");
                                }
                            }

                            if (idStatus == Fetch.STATUS_DOWNLOADING) {

                                storeEntireDetails = StoreEntireDetails.getInstance(mContext);
                                boolean IsEntry = storeEntireDetails.updateDownloadData(subject, String.valueOf(fetch.get(Long.parseLong(downloadId)).getProgress()), "onProgress");

                                if (IsEntry) {
                                    Log.d("##status", "Successfully Updated");

                                } else {
                                    Log.d("##status", "Update failed");
                                }
                            }

                            if (idStatus == Fetch.STATUS_ERROR) {

                                fetch.retry(fetch.get(Long.parseLong(downloadId)).getId());
                                storeEntireDetails = StoreEntireDetails.getInstance(mContext);
                                boolean IsEntry = storeEntireDetails.updateDownloadData(subject, String.valueOf(fetch.get(Long.parseLong(downloadId)).getProgress()), "onFailed");

                                if (IsEntry) {
                                    Log.d("##status", "Successfully Updated");

                                } else {
                                    Log.d("##status", "Update failed");
                                }
                            }
                        }
                    }
                }
            }
        };
        fetchThread.start();

        //        Add Image for card
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            public void run() {

//                Add Image for card
                try {
                    File mydir = mContext.getDir("Chathamkulam", Context.MODE_PRIVATE); //Creating an internal dir;
                    File rootPath = new File(mydir, subject);
                    File rootPathh = new File(rootPath, "@" + subject + ".jpg");
//                    Log.d("Filename", String.valueOf(rootPath));

                    if (!rootPathh.exists()) {

                        handler.post(new Runnable() {
                            public void run() {

                                holder.subject_image.setBackgroundResource(android.R.drawable.ic_dialog_alert);
                                holder.txtSubjectName.setVisibility(View.VISIBLE);
                                holder.txtSubjectName.setText(subject);

                            }
                        });

                    } else {

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;

                        final Bitmap mBitmap = BitmapFactory.decodeFile(rootPathh.getAbsolutePath(), options);

                        handler.post(new Runnable() {
                            public void run() {

                                holder.subject_image.setImageBitmap(mBitmap);

                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Exception on Bitmap", e.getMessage());
                }
            }
        };
        new Thread(runnable).start();

        String video = dashEntityObjects.getVideo_count();
        if (video.equals("0")) {
            holder.iocVideo.setVisibility(View.GONE);
            holder.txtVideo.setVisibility(View.GONE);
        }

        final String notes = dashEntityObjects.getNotes_count();
        if (notes.equals("0")) {
            holder.iocNotes.setVisibility(View.GONE);
            holder.txtNotes.setVisibility(View.GONE);
        }

        String qBank = dashEntityObjects.getQbank_count();
        if (qBank.equals("0")) {
            holder.iocQuestionBank.setVisibility(View.GONE);
            holder.txtQbank.setVisibility(View.GONE);
        }

        String videoSize = dashEntityObjects.getDuration();

        String[] sizes = videoSize.split(":");
        String hours = sizes[0];
        String minute = sizes[1];
        String seconds = sizes[2];

        if (hours.equals("00")) {
            if (!minute.equals("00")) {
                holder.txtVideoDuration.setText("(" + minute + ":" + seconds + " Mins" + ")");
            } else {
                holder.txtVideoDuration.setText("(" + minute + ":" + seconds + " Secs" + ")");
            }
        } else {
            holder.txtVideoDuration.setText("(" + videoSize + " Hrs" + ")");
        }

        final String validity = dashEntityObjects.getValidityTill();

        Thread thread1 = new Thread() {
            public void run() {

                Intent intent = new Intent(mContext, AlarmReceiver.class);
                intent.putExtra("Key_currentSub", subject);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);

                Calendar calendar = Calendar.getInstance();

                String[] sep = validity.split("/");

                int day = Integer.parseInt(sep[0]);
                int month = Integer.parseInt(sep[1]);
                int year = Integer.parseInt(sep[2]);

                calendar.set(year, month, day);

                AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

//                    Log.v("Alarm Result","Alarm set with"+"   "+calendar.getTimeInMillis());
            }
        };
        thread1.start();

        String status = dashEntityObjects.getStatus();
        String progress = dashEntityObjects.getProgress();

        Log.v("details", status + "   " + progress);

        holder.dashProgressBar.setProgress(Integer.parseInt(progress));

        if (status.equals("onCompleted") && progress.equals("100")) {
            holder.txtDownloadStatus.setText("Downloaded");

            Thread thread = new Thread() {
                public void run() {

                    File mydir = mContext.getDir("Chathamkulam", Context.MODE_PRIVATE); //Creating an internal dir;
                    File myZipFile = new File(mydir, subject + ".zip"); //Getting a file within the dir.

                    File myUnZipFile = new File(mydir, subject);

                    if (myZipFile.exists()) {
//                        Log.d("fileDetails", "file exists!"+"  "+myZipFile);

                        try {
                            unzip(myZipFile, myUnZipFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            thread.start();

        } else {

            holder.subject_image.setBackgroundResource(android.R.drawable.ic_dialog_alert);
            holder.txtSubjectName.setVisibility(View.VISIBLE);
            holder.txtSubjectName.setText(subject);

        }
        if (status.equals("onFailed")) {
            holder.txtDownloadStatus.setText("Failed");
        }

        if (status.equals("onDownloadPaused")) {
            holder.txtDownloadStatus.setText("Paused");
        }

        if (status.equals("onDownloadCanceled")) {

            File mydir = mContext.getDir("Chathamkulam", Context.MODE_PRIVATE);
            final File rootPath = new File(mydir, subject + ".zip"); //Getting a file within the dir.

            if (rootPath.exists()) {
                deleteRecursive(rootPath);
                Log.d("cancelReport", "fileSuccessfully Removed");
            }

            StoreEntireDetails storeEntireDetails = StoreEntireDetails.getInstance(mContext);
            storeEntireDetails.removeExpireSubject(subject);
        }

        holder.iocVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Use bounce interpolator with amplitude 0.2 and frequency 20
                Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                myAnim.setInterpolator(interpolator);
                holder.iocVideo.startAnimation(myAnim);

                currentSubName = dashEntityObjects.getSubject_name();
//                Toast.makeText(mContext,dashData.getSubject(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, ModuleList.class);
                intent.putExtra("Key_video", currentSubName);
                mContext.startActivity(intent);

            }
        });

        holder.iocNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Use bounce interpolator with amplitude 0.2 and frequency 20
                Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                myAnim.setInterpolator(interpolator);
                holder.iocNotes.startAnimation(myAnim);

                currentSubName = dashEntityObjects.getSubject_name();

//                Toast.makeText(mContext,"This is notesViewer",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, NSPDFViewer.class);
                intent.putExtra("Key_pdf", currentSubName);
                mContext.startActivity(intent);

            }
        });

        holder.iocQuestionBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Use bounce interpolator with amplitude 0.2 and frequency 20
                Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                myAnim.setInterpolator(interpolator);
                holder.iocQuestionBank.startAnimation(myAnim);

                currentSubName = dashEntityObjects.getSubject_name();
//                Toast.makeText(mContext,"This is QuestionBank"+"   "+currentSubName,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, QBPDFViewer.class);
                intent.putExtra("Key_pdf", currentSubName);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    mFilteredList = myList;
                } else {

                    ArrayList<DashEntityObjects> filteredList = new ArrayList<>();

                    for (DashEntityObjects dashEntityObjects : myList) {

                        if (dashEntityObjects.getCountry().toLowerCase().contains(charString) ||
                                dashEntityObjects.getSemester().toLowerCase().contains(charString) ||
                                dashEntityObjects.getUniversity().toLowerCase().contains(charString) ||
                                dashEntityObjects.getCourse().toLowerCase().contains(charString) ||
                                dashEntityObjects.getSubject_name().toLowerCase().contains(charString)) {

                            filteredList.add(dashEntityObjects);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<DashEntityObjects>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTrail, txtPrice, txtValid, txtVideoDuration, txtVideo, txtNotes, txtQbank, txtSubjectName, txtDownloadStatus;
        ImageView subject_image, iocVideo, iocNotes, iocQuestionBank;
        ProgressBar dashProgressBar;

        MyViewHolder(View view) {
            super(view);
            txtTrail = (TextView) view.findViewById(R.id.txtTrail);
            txtPrice = (TextView) view.findViewById(R.id.txtPrice);
            txtValid = (TextView) view.findViewById(R.id.txtValid);
            txtVideoDuration = (TextView) view.findViewById(R.id.txtVideoDuration);
            txtVideo = (TextView) view.findViewById(R.id.txtVideo);
            txtNotes = (TextView) view.findViewById(R.id.txtNotes);
            txtQbank = (TextView) view.findViewById(R.id.txtQbank);

            txtSubjectName = (TextView) view.findViewById(R.id.txtSubjectName);
            txtSubjectName.setVisibility(View.GONE);

            subject_image = (ImageView) view.findViewById(R.id.ime_description);
            iocVideo = (ImageView) view.findViewById(R.id.iocVideo);
            iocNotes = (ImageView) view.findViewById(R.id.iocNotes);
            iocQuestionBank = (ImageView) view.findViewById(R.id.iocQB);

            dashProgressBar = (ProgressBar) view.findViewById(R.id.dashProgressBar);
            txtDownloadStatus = (TextView) view.findViewById(R.id.txtDownloadStatus);

        }
    }
}
