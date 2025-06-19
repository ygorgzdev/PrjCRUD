package com.example.prjcrud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prjcrud.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements DbAmigosAdapter.QuantidadeListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Intent intent = getIntent();
        if(intent.hasExtra("amigo")){
            findViewById(R.id.include_cadastrar_amigo).setVisibility(View.VISIBLE);
            findViewById(R.id.include_listar_amigos).setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            amigoAlterado = (DbAmigo) intent.getSerializableExtra("amigo");
            EditText edtNome     = (EditText)findViewById(R.id.edtNome);
            EditText edtCelular  = (EditText)findViewById(R.id.edtCelular);
            EditText edtLatitude = (EditText)findViewById(R.id.edtLatitude);
            EditText edtLongitude = (EditText)findViewById(R.id.edtLongitude);

            edtNome.setText(amigoAlterado.getNome());
            edtCelular.setText(amigoAlterado.getCelular());
            int status = 2;
        }

/*
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration); */

        // Novo código do FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.include_listar_amigos).setVisibility(View.INVISIBLE);
                findViewById(R.id.include_cadastrar_amigo).setVisibility(View.VISIBLE);
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            }
        });

        Button btnCancelar = (Button) findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Cancelando...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                findViewById(R.id.include_listar_amigos).setVisibility(View.VISIBLE);
                findViewById(R.id.include_cadastrar_amigo).setVisibility(View.INVISIBLE);
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
            }
        });

        Button btnSalvar = (Button) findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edtNome = (EditText) findViewById(R.id.edtNome);
                EditText edtCelular = (EditText) findViewById(R.id.edtCelular);
                EditText edtLatitude = (EditText) findViewById(R.id.edtLatitude);
                EditText edtLongitude = (EditText) findViewById(R.id.edtLongitude);

                String nome = edtNome.getText().toString().trim();
                String celular = edtCelular.getText().toString().trim();
                String latitude = edtLatitude.getText().toString().trim();
                String longitude = edtLongitude.getText().toString().trim();

                // Validações básicas
                if (nome.isEmpty()) {
                    Snackbar.make(view, "Por favor, informe o nome do amigo!", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();
                    edtNome.requestFocus();
                    return;
                }

                if (celular.isEmpty()) {
                    Snackbar.make(view, "Por favor, informe o número do celular!", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();
                    edtCelular.requestFocus();
                    return;
                }
                if (!validarCelularAnatel(celular)) {
                    Snackbar.make(view, "Número de celular inválido! Use o formato: (XX) 9XXXX-XXXX", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();
                    edtCelular.requestFocus();
                    return;
                }

                String celularFormatado = formatarCelular(celular);

                DbAmigosDAO dao = new DbAmigosDAO(getBaseContext());
                boolean sucesso;
                if (amigoAlterado != null) {
                    sucesso = dao.salvar(amigoAlterado.getId(), nome, celularFormatado, latitude, longitude, 2);
                } else {
                    sucesso = dao.salvar(nome, celularFormatado, latitude, longitude, 1);
                }
                if (sucesso) {
                    DbAmigo amigo = dao.ultimoAmigo();

                    if (amigoAlterado != null) {
                        adapter.atualizarAmigo(amigo);
                        amigoAlterado = null;

                        configurarRecycler();
                    } else {
                        adapter.inserirAmigo(amigo);
                    }

                    Snackbar.make(view, "Dados de [" + nome + "] salvos com sucesso!", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();

                    edtNome.setText("");
                    edtCelular.setText("");
                    edtLatitude.setText("");
                    edtLongitude.setText("");

                    findViewById(R.id.include_listar_amigos).setVisibility(View.VISIBLE);
                    findViewById(R.id.include_cadastrar_amigo).setVisibility(View.INVISIBLE);
                    findViewById(R.id.fab).setVisibility(View.VISIBLE);

                } else {
                    Snackbar.make(view, "Erro ao salvar, consulte o log!", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();
                }
            }
        });
        configurarRecycler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_delete_all) {
            confirmarExclusaoTodos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmarExclusaoTodos() {
        DbAmigosDAO dao = new DbAmigosDAO(this);
        int quantidade = dao.contarAmigos();

        if (quantidade == 0) {
            Snackbar.make(findViewById(android.R.id.content), "Não há amigos cadastrados para excluir!", Snackbar.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação de Exclusão")
                .setMessage("Tem certeza que deseja excluir TODOS os " + quantidade + " amigo(s) cadastrado(s)?\n\nEsta ação não pode ser desfeita!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Excluir Todos", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        excluirTodosAmigos();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    private void excluirTodosAmigos() {
        DbAmigosDAO dao = new DbAmigosDAO(this);
        boolean sucesso = dao.excluirTodos();

        if (sucesso) {
            adapter.limparTodos();
            Snackbar.make(findViewById(android.R.id.content), "Todos os amigos foram excluídos com sucesso!", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Erro ao excluir todos os amigos!", Snackbar.LENGTH_LONG).show();
        }
    }

/*
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    RecyclerView recyclerView;
    DbAmigosAdapter adapter;

    private void configurarRecycler() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DbAmigosDAO dao = new DbAmigosDAO(this);
        adapter = new DbAmigosAdapter(dao.listarAmigos());
        adapter.setQuantidadeListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        atualizarQuantidadeAmigos();
    }
    private void atualizarQuantidadeAmigos() {
        TextView txvQuantidadeAmigos = findViewById(R.id.txvQuantidadeAmigos);
        if (txvQuantidadeAmigos != null && adapter != null) {
            int quantidade = adapter.getItemCount();
            txvQuantidadeAmigos.setText("Total de amigos cadastrados: " + quantidade);
        }
    }
    @Override
    public void onQuantidadeChanged() {
        atualizarQuantidadeAmigos();
    }

    private boolean validarCelularAnatel(String celular) {
        if (celular == null || celular.trim().isEmpty()) {
            return false;
        }

        String numeroLimpo = celular.replaceAll("[^0-9]", "");

        if (numeroLimpo.length() != 11) {
            return false;
        }

        String ddd = numeroLimpo.substring(0, 2);
        int dddInt = Integer.parseInt(ddd);

        if (dddInt < 11 || dddInt > 99) {
            return false;
        }

        char terceiroDigito = numeroLimpo.charAt(2);
        if (terceiroDigito != '9') {
            return false;
        }

        char quartoDigito = numeroLimpo.charAt(3);
        if (quartoDigito < '6' || quartoDigito > '9') {
            return false;
        }

        return true;
    }

    private String formatarCelular(String celular) {
        if (celular == null) return "";

        String numeroLimpo = celular.replaceAll("[^0-9]", "");

        if (numeroLimpo.length() == 11) {
            return String.format("(%s) %s%s%s%s%s-%s%s%s%s",
                    numeroLimpo.substring(0, 2),
                    numeroLimpo.charAt(2),
                    numeroLimpo.charAt(3),
                    numeroLimpo.charAt(4),
                    numeroLimpo.charAt(5),
                    numeroLimpo.charAt(6),
                    numeroLimpo.charAt(7),
                    numeroLimpo.charAt(8),
                    numeroLimpo.charAt(9),
                    numeroLimpo.charAt(10)
            );
        }
        return celular;
    }
    DbAmigo amigoAlterado = null;
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i=0;(i<spinner.getCount())&&!(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString));i++);
        return index;
    }

}