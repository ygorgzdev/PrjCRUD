package com.example.prjcrud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class DbAmigosAdapter extends RecyclerView.Adapter<DbAmigosHolder> {

    private final List<DbAmigo> amigos;
    private final MainActivity mainActivity;

    public DbAmigosAdapter(List<DbAmigo> amigos, MainActivity mainActivity) {
        this.amigos = amigos;
        this.mainActivity = mainActivity;
    }

    @Override
    public DbAmigosHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DbAmigosHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_dados_amigo, parent, false));
    }

    @Override
    public void onBindViewHolder(DbAmigosHolder holder, int position) {
        DbAmigo amigo = amigos.get(position);

        holder.txvNome.setText(amigo.getNome());
        holder.txvCelular.setText(amigo.getCelular());
        holder.txvLatitude.setText(amigo.getLatitude());
        holder.txvLongitude.setText(amigo.getLongitude());

        // Configurar botão Editar
        holder.btnEditar.setOnClickListener(v -> {
            Activity activity = getActivity(v);
            Intent intent = activity.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("amigo", amigo);
            activity.finish();
            activity.startActivity(intent);
        });

        // Configurar botão Excluir
        holder.btnExcluir.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Confirmação")
                    .setMessage("Tem certeza que deseja excluir o amigo [" + amigo.getNome() + "]?")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        DbAmigosDAO dao = new DbAmigosDAO(v.getContext());
                        boolean sucesso = dao.excluir(amigo.getId());
                        if (sucesso) {
                            Snackbar.make(v, "Excluindo o amigo [" + amigo.getNome() + "]!",
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            excluirAmigo(amigo);
                        } else {
                            Snackbar.make(v, "Erro ao excluir o amigo [" + amigo.getNome() + "]!",
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        });

        // Configurar botão SMS
        holder.btnSms.setOnClickListener(v -> {
            mostrarDialogoSMS(amigo);
        });

        // Configurar botão Ligar
        holder.btnLigar.setOnClickListener(v -> {
            if (mainActivity != null) {
                mainActivity.fazerLigacao(amigo.getCelular());
            }
        });

        // Configurar botão WhatsApp
        holder.btnWhats.setOnClickListener(v -> {
            if (mainActivity != null) {
                mainActivity.abrirWhatsApp(amigo.getCelular());
            }
        });
    }

    private void mostrarDialogoSMS(DbAmigo amigo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Enviar SMS para " + amigo.getNome());

        // Criar EditText para a mensagem
        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Digite sua mensagem...");
        input.setLines(4);
        input.setMaxLines(6);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String mensagem = input.getText().toString().trim();
            if (!mensagem.isEmpty()) {
                if (mainActivity != null) {
                    mainActivity.enviarSMS(amigo.getCelular(), mensagem);
                }
            } else {
                Snackbar.make(mainActivity.findViewById(android.R.id.content),
                        "Mensagem não pode estar vazia", Snackbar.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return amigos != null ? amigos.size() : 0;
    }

    public void inserirAmigo(DbAmigo amigo) {
        amigos.add(amigo);
        notifyItemInserted(getItemCount() - 1);
    }

    public void atualizarAmigo(DbAmigo amigo) {
        int posicao = amigos.indexOf(amigo);
        if (posicao != -1) {
            amigos.set(posicao, amigo);
            notifyItemChanged(posicao);
        }
    }

    public void excluirAmigo(DbAmigo amigo) {
        int position = amigos.indexOf(amigo);
        if (position != -1) {
            amigos.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}