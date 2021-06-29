package ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.self.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;
    public RecyclerViewAdapter(Context context,List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        holder.rowtitle.setText(journal.getTitle());
        holder.rowthought.setText(journal.getThought());
        String imageUrl = journal.getImageURL();
        holder.rowusername.setText(journal.getUsername());
        /*
        * Picasso.get().load().placeholder().into();
        * Using Picasso library to get the image from the image URL and set it into image view
        */
        Picasso.get().load(imageUrl).
                fit().placeholder(R.drawable.one_image).into(holder.rowbackground);

        /*
        * Using time ago format for displaying time
        * for e.g 1 hour ago,3 minutes ago...
        */
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds()*1000);
        holder.rowdate.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton shareButton;
        public ImageView rowbackground;
        public TextView rowtitle,rowthought,rowdate,rowusername;
        public ViewHolder(@NonNull View itemView,Context ctx) {
            super(itemView);
            context = ctx;
            rowbackground = itemView.findViewById(R.id.row_backgorund);
            rowthought = itemView.findViewById(R.id.row_thought);
            rowtitle = itemView.findViewById(R.id.row_title);
            rowdate = itemView.findViewById(R.id.row_date);
            rowusername = itemView.findViewById(R.id.row_username);

            shareButton = itemView.findViewById(R.id.row_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT,journalList.get(getAdapterPosition()).getTitle());
                    intent.putExtra(Intent.EXTRA_TEXT,journalList.get(getAdapterPosition()).getThought());
                    intent.setType("text/plain");
                    context.startActivity(intent);
                }
            });
        }
    }
}
