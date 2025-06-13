package com.example.prjcrud;

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

public class MainActivity extends AppCompatActivity {

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
                // Sincronizando os campos com o contexto
                EditText edtNome = (EditText) findViewById(R.id.edtNome);
                EditText edtCelular = (EditText) findViewById(R.id.edtCelular);
                EditText edtLatitude = (EditText) findViewById(R.id.edtLatitude);
                EditText edtLongitude = (EditText) findViewById(R.id.edtLongitude);

                // Adaptando atributos
                String nome = edtNome.getText().toString();
                String celular = edtCelular.getText().toString();
                String latitude = edtLatitude.getText().toString();
                String longitude = edtLongitude.getText().toString();
                int situacao = 1;

// Gravando no banco de dados
                DbAmigosDAO dao = new DbAmigosDAO(getBaseContext());
                boolean sucesso = dao.salvar(nome, celular, latitude, longitude, situacao);

                if (sucesso) {

                    DbAmigo amigo = dao.ultimoAmigo();
                    adapter.inserirAmigo(amigo);

                    Snackbar.make(view, "Dados de [" + nome + "] salvos com sucesso!", Snackbar.LENGTH_LONG)
                            .setAction("Ação", null).show();

                    // Inicializando os campos do contexto
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

        return super.onOptionsItemSelected(item);
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
        // Ativando o layou para uma lista tipo RecyclerView e configurando-a

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DbAmigosDAO dao = new DbAmigosDAO(this);
        adapter = new DbAmigosAdapter(dao.listarAmigos());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    DbAmigo amigoAlterado = null;
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i=0;(i<spinner.getCount())&&!(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString));i++);
        return index;
    }



}
