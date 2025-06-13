package com.example.prjcrud;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DbAmigosAdapter extends RecyclerView.Adapter<DbAmigosHolder> {

    private final List<DbAmigo> amigos;

    public DbAmigosAdapter(List<DbAmigo> amigos) {
        this.amigos = amigos;
    }


    @Override
    public DbAmigosHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DbAmigosHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_dados_amigo, parent, false));
    }
    @Override
    public void onBindViewHolder(DbAmigosHolder holder, int position) {
        holder.txvNome.setText(amigos.get(position).getNome());
        holder.txvCelular.setText(amigos.get(position).getCelular());
        holder.txvLatitude.setText(amigos.get(position).getLatitude());
        holder.txvLongitude.setText(amigos.get(position).getLongitude());
    }
    @Override
    public int getItemCount() {
        return amigos != null ? amigos.size() : 0;
    }
    public void inserirAmigo(DbAmigo amigo){
        amigos.add(amigo);
        notifyItemInserted(getItemCount());
    }

}


