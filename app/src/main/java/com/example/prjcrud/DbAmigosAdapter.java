package com.example.prjcrud;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class DbAmigosAdapter extends RecyclerView.Adapter<DbAmigosHolder> {

    private final List<DbAmigo> amigos;
    private QuantidadeListener quantidadeListener;

    public interface QuantidadeListener {
        void onQuantidadeChanged();
    }

    public DbAmigosAdapter(List<DbAmigo> amigos) {
        this.amigos = amigos;
    }

    public void setQuantidadeListener(QuantidadeListener listener) {
        this.quantidadeListener = listener;
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
        holder.btnEditar.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity(v);
                Intent intent = activity.getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("amigo", amigos.get(position));
                activity.finish();
                activity.startActivity(intent);
            }
        });

        final DbAmigo amigo = amigos.get(position);
        holder.btnExcluir.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = v;
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Confirmação")
                        .setMessage("Tem certeza que deseja excluir o amigo ["+amigo.getNome().toString()+"]?")
                        .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbAmigo amigo = amigos.get(position);
                                DbAmigosDAO dao = new DbAmigosDAO(view.getContext());

                                boolean sucesso = dao.excluir(amigo.getId());
                                if(sucesso) {
                                    Snackbar.make(view, "Excluindo o amigo ["+amigo.getNome().toString()+"]!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    excluirAmigo(amigo);
                                }else{
                                    Snackbar.make(view, "Erro ao excluir o amigo ["+amigo.getNome().toString()+"]!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create()
                        .show();
            }
        });

        holder.btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarSMS(v.getContext(), amigo);
            }
        });

        holder.btnLigar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerLigacao(v.getContext(), amigo);
            }
        });

        holder.btnWhats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirWhatsApp(v.getContext(), amigo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return amigos != null ? amigos.size() : 0;
    }

    public void inserirAmigo(DbAmigo amigo){
        amigos.add(amigo);
        notifyItemInserted(getItemCount());
        if (quantidadeListener != null) {
            quantidadeListener.onQuantidadeChanged();
        }
    }

    public void atualizarAmigo(DbAmigo amigo){
        amigos.set(amigos.indexOf(amigo), amigo);
        notifyItemChanged(amigos.indexOf(amigo));
        if (quantidadeListener != null) {
            quantidadeListener.onQuantidadeChanged();
        }
    }

    public void excluirAmigo(DbAmigo amigo)
    {
        int position = amigos.indexOf(amigo);
        amigos.remove(position);
        notifyItemRemoved(position);
        if (quantidadeListener != null) {
            quantidadeListener.onQuantidadeChanged();
        }
    }

    public void limparTodos() {
        amigos.clear();
        notifyDataSetChanged();
        if (quantidadeListener != null) {
            quantidadeListener.onQuantidadeChanged();
        }
    }

    private Activity getActivity(View view) {
        Context context = view.getContext();

        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    private void enviarSMS(Context context, DbAmigo amigo) {
        try {

            String numeroLimpo = amigo.getCelular().replaceAll("[^0-9]", "");
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + numeroLimpo));
            smsIntent.putExtra("sms_body", "Olá " + amigo.getNome() + "! Como você está?");

            if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(smsIntent);
            } else {
                Toast.makeText(context, "Nenhum aplicativo de SMS encontrado!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao abrir SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fazerLigacao(Context context, DbAmigo amigo) {
        try {

            String numeroLimpo = amigo.getCelular().replaceAll("[^0-9]", "");
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + numeroLimpo));

            if (callIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(callIntent);
            } else {
                Toast.makeText(context, "Nenhum aplicativo de telefone encontrado!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao fazer ligação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirWhatsApp(Context context, DbAmigo amigo) {
        try {
            String numeroLimpo = amigo.getCelular().replaceAll("[^0-9]", "");

            if (!numeroLimpo.startsWith("55")) {
                numeroLimpo = "55" + numeroLimpo;
            }

            String mensagem = "Olá " + amigo.getNome() + "! Como você está?";

            try {
                Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + numeroLimpo + "&text=" + Uri.encode(mensagem)));
                whatsappIntent.setPackage("com.whatsapp");

                if (whatsappIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(whatsappIntent);
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + numeroLimpo + "&text=" + Uri.encode(mensagem)));

                    if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(browserIntent);
                    } else {
                        Toast.makeText(context, "WhatsApp não instalado e nenhum navegador encontrado!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://web.whatsapp.com/send?phone=" + numeroLimpo + "&text=" + Uri.encode(mensagem)));

                if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "Erro ao abrir WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao abrir WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}