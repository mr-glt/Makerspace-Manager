package org.utmakersociety.makerspacemanager.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.QuerySnapshot;

import org.utmakersociety.makerspacemanager.R;
import org.utmakersociety.makerspacemanager.activities.MainActivity;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private QuerySnapshot users;
    private Context context;

    public UsersAdapter(QuerySnapshot users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card,
                parent, false));
    }

    @Override
    public void onBindViewHolder (@NonNull ViewHolder holder, int position){
        holder.setIsRecyclable(false);
        holder.nameTV.setText(Objects.requireNonNull(users.getDocuments().get(position)
                .get("name")).toString());
        holder.certLevelTV.setText("Level " + Objects.requireNonNull(users.getDocuments()
                .get(position).get("certLevel")).toString());
        if ((boolean)users.getDocuments().get(position).get("employee"))
            holder.employeeChip.setVisibility(View.VISIBLE);
        if ((boolean)users.getDocuments().get(position).get("seniorDesign"))
            holder.seniorChip.setVisibility(View.VISIBLE);
        if ((boolean)users.getDocuments().get(position).get("freshmanDesign"))
            holder.freshmanChip.setVisibility(View.VISIBLE);
        if ((boolean)users.getDocuments().get(position).get("studentOrg")){
            holder.orgChip.setVisibility(View.VISIBLE);
            holder.orgChip.setChipText(Objects.requireNonNull(users.getDocuments().get(position).
                    get("studentOrgName")).toString());
        }
        if ((boolean)users.getDocuments().get(position).get("admin"))
            holder.adminChip.setVisibility(View.VISIBLE);
        if ((boolean)users.getDocuments().get(position).get("cert"))
            holder.certChip.setVisibility(View.VISIBLE);
        if ((boolean)users.getDocuments().get(position).get("ms"))
            holder.adminChip.setVisibility(View.VISIBLE);
        String major = Objects.requireNonNull(users.getDocuments().get(position)
                .get("major")).toString();

        switch (major) {
            case "EECS":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_memory_24, context.getTheme()));
                holder.majorChip.setChipText("EECS");
                break;
            case "BIOE":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_bug_report_24, context.getTheme()));
                holder.majorChip.setChipText("BIOE");
                break;
            case "MIME":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_build_24, context.getTheme()));
                holder.majorChip.setChipText("MIME");
                break;
            case "CHEME":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_opacity_24, context.getTheme()));
                holder.majorChip.setChipText("CHEME");
                break;
            case "ENGT":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_developer_board_24, context.getTheme()));
                holder.majorChip.setChipText("ENGT");
                break;
            case "CIVE":
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_terrain_24, context.getTheme()));
                holder.majorChip.setChipText("CIVE");
                break;
            default:
                holder.majorChip.setChipIcon(context.getResources().getDrawable(
                        R.drawable.baseline_book_24, context.getTheme()));
                holder.majorChip.setChipText("Other Major");
        }
        holder.card.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        holder.card.setOnClickListener(view -> {
            if (holder.checkIn.getVisibility()==View.GONE){
                holder.checkIn.setVisibility(View.VISIBLE);
                holder.edit.setVisibility(View.VISIBLE);
            }else{
                holder.checkIn.setVisibility(View.GONE);
                holder.edit.setVisibility(View.GONE);
            }
        });

        holder.adminChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                    new com.pchmn.materialchips.model.Chip(7,context.getDrawable
                    (R.drawable.baseline_vpn_key_24),context.getString(R.string.admin)
                            ,"Has authority over the database"));
            } catch (Exception ignored) {
            }
        });
        holder.certChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                    new com.pchmn.materialchips.model.Chip(8,context.getDrawable
                        (R.drawable.baseline_assignment_24),context.getString(R.string.certifier)
                            ,"Has authority certification level"));
            } catch (Exception ignored) {
            }
        });
        holder.employeeChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                    new com.pchmn.materialchips.model.Chip(9,context.getDrawable
                    (R.drawable.baseline_work_24),context.getString(R.string.employee)
                            ,"Employed to manage the Makerspace"));
            } catch (Exception ignored) {
            }
        });
        holder.seniorChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                    new com.pchmn.materialchips.model.Chip(11,context.getDrawable
                        (R.drawable.baseline_school_24),context.getString(R.string.senior_design)
                        ,"Member of senior design"));
            } catch (Exception ignored) {
            }
        });
        holder.freshmanChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                    new com.pchmn.materialchips.model.Chip(10,context.getDrawable
                        (R.drawable.baseline_child_friendly_24),context.getString(R.string.freshman_design)
                        ,"Member of freshman design"));
            } catch (Exception ignored) {
            }
        });
        holder.orgChip.setOnClickListener(view -> {
            try {
                ((MainActivity) view.getContext()).addChip(
                        new com.pchmn.materialchips.model.Chip(12,context.getDrawable
                                (R.drawable.baseline_account_balance_24),context
                                .getString(R.string.student_organization)
                                ,"Representative of another student organization"));
            } catch (Exception ignored) {
            }
        });
        holder.checkIn.setOnClickListener(view -> {
            ((MainActivity) view.getContext()).checkInUser(users.getDocuments().get(position).getId()
                ,Objects.requireNonNull(users.getDocuments().get(position).get("name")).toString());
            holder.checkIn.setVisibility(View.GONE);
            holder.edit.setVisibility(View.GONE);
        });
    }

    @Override
    public int getItemCount () {
    return users.getDocuments().size();
}

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView nameTV;
        TextView certLevelTV;
        Chip employeeChip;
        Chip msChip;
        Chip adminChip;
        Chip certChip;
        Chip freshmanChip;
        Chip seniorChip;
        Chip orgChip;
        Chip majorChip;
        MaterialButton checkIn;
        MaterialButton edit;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            nameTV = itemView.findViewById(R.id.user_name);
            certLevelTV = itemView.findViewById(R.id.certLevel);
            employeeChip = itemView.findViewById(R.id.employeeChip);
            msChip = itemView.findViewById(R.id.msChip);
            adminChip = itemView.findViewById(R.id.adminChip);
            certChip = itemView.findViewById(R.id.certChip);
            freshmanChip = itemView.findViewById(R.id.freshmanChip);
            seniorChip = itemView.findViewById(R.id.seniorChip);
            orgChip = itemView.findViewById(R.id.orgChip);
            majorChip = itemView.findViewById(R.id.majorChip);
            checkIn = itemView.findViewById(R.id.checkIn);
            edit = itemView.findViewById(R.id.edit);

        }

    }
}
