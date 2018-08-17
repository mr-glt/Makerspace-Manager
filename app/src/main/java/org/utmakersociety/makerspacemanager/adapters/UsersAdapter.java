package org.utmakersociety.makerspacemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.QuerySnapshot;

import org.utmakersociety.makerspacemanager.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private QuerySnapshot users;

    public UsersAdapter(QuerySnapshot users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card,
                parent, false));
    }

    @Override
    public void onBindViewHolder (@NonNull ViewHolder holder, int position){
    holder.nameTV.setText(Objects.requireNonNull(users.getDocuments().get(position)
            .get("name")).toString());

    }
        @Override
        public int getItemCount () {
        return users.getDocuments().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV;

        ViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.user_name);
        }
    }
}
