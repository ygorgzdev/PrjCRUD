package com.example.prjcrud;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Patterns;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prjcrud.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private DbAmigosAdapter adapter;
    private DbAmigo amigoAlterado = null;
    private TextView txtTotalAmigos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Solicitar permissões necessárias
        solicitarPermissoes();

        Intent intent = getIntent();
        if(intent.hasExtra("amigo")){
            findViewById(R.id.include_cadastrar_amigo).setVisibility(View.VISIBLE);
            findViewById(R.id.include_listar_amigos).setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            amigoAlterado = (DbAmigo) intent.getSerializableExtra("amigo");

            EditText edtNome = findViewById(R.id.edtNome);
            EditText edtCelular = findViewById(R.id.edtCelular);
            EditText edtLatitude = findViewById(R.id.edtLatitude);
            EditText edtLongitude = findViewById(R.id.edtLongitude);

            edtNome.setText(amigoAlterado.getNome());
            edtCelular.setText(amigoAlterado.getCelular());
            edtLatitude.setText(amigoAlterado.getLatitude());
            edtLongitude.setText(amigoAlterado.getLongitude());
        }

        // Configurar FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            esconderListaAmigos();
            findViewById(R.id.include_listar_amigos).setVisibility(View.INVISIBLE);
            findViewById(R.id.include_cadastrar_amigo).setVisibility(View.VISIBLE);
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        });

        // Configurar botão Cancelar
        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(view -> {
            limparCampos();
            esconderCadastroAmigo();
            amigoAlterado = null;
        });

        // Configurar botão Salvar
        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(view -> salvarAmigo());

        configurarRecycler();
        atualizarContadorAmigos();
    }

    private void solicitarPermissoes() {
        String[] permissoes = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE
        };

        for (String permissao : permissoes) {
            if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissoes, PERMISSION_REQUEST_CODE);
                break;
            }
        }
    }

    private void salvarAmigo() {
        EditText edtNome = findViewById(R.id.edtNome);
        EditText edtCelular = findViewById(R.id.edtCelular);
        EditText edtLatitude = findViewById(R.id.edtLatitude);
        EditText edtLongitude = findViewById(R.id.edtLongitude);

        String nome = edtNome.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String latitude = edtLatitude.getText().toString().trim();
        String longitude = edtLongitude.getText().toString().trim();

        // Validação dos campos
        if (nome.isEmpty()) {
            edtNome.setError("Nome é obrigatório");
            edtNome.requestFocus();
            return;
        }

        if (celular.isEmpty()) {
            edtCelular.setError("Celular é obrigatório");
            edtCelular.requestFocus();
            return;
        }

        // Validação do número de celular brasileiro
        if (!validarCelularBrasileiro(celular)) {
            edtCelular.setError("Número de celular inválido. Use o formato: (XX) 9XXXX-XXXX");
            edtCelular.requestFocus();
            return;
        }

        DbAmigosDAO dao = new DbAmigosDAO(getBaseContext());
        boolean sucesso;

        if (amigoAlterado != null) {
            sucesso = dao.salvar(amigoAlterado.getId(), nome, celular, latitude, longitude, 2);
        } else {
            sucesso = dao.salvar(nome, celular, latitude, longitude, 1);
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

            Snackbar.make(findViewById(android.R.id.content),
                            "Dados de [" + nome + "] salvos com sucesso!", Snackbar.LENGTH_LONG)
                    .setAction("Ação", null).show();

            limparCampos();
            esconderCadastroAmigo();
            atualizarContadorAmigos();

        } else {
            Snackbar.make(findViewById(android.R.id.content),
                            "Erro ao salvar, consulte o log!", Snackbar.LENGTH_LONG)
                    .setAction("Ação", null).show();
        }
    }

    private boolean validarCelularBrasileiro(String celular) {
        // Remove caracteres especiais
        String numerosApenas = celular.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos (DDD + 9 dígitos)
        if (numerosApenas.length() != 11) {
            return false;
        }

        // Verifica se o terceiro dígito é 9 (celular)
        if (numerosApenas.charAt(2) != '9') {
            return false;
        }

        // Verifica se o DDD é válido (11 a 99)
        int ddd = Integer.parseInt(numerosApenas.substring(0, 2));
        return ddd >= 11 && ddd <= 99;
    }

    public void enviarSMS(String numeroTelefone, String mensagem) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                String numeroLimpo = numeroTelefone.replaceAll("[^0-9]", "");
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(numeroLimpo, null, mensagem, null, null);

                Toast.makeText(this, "SMS enviado com sucesso!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao enviar SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Permissão para enviar SMS não concedida", Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirWhatsApp(String numeroTelefone) {
        try {
            String numeroLimpo = numeroTelefone.replaceAll("[^0-9]", "");
            // Adiciona código do país (55) se não tiver
            if (!numeroLimpo.startsWith("55")) {
                numeroLimpo = "55" + numeroLimpo;
            }

            String url = "https://wa.me/" + numeroLimpo;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp não instalado ou erro ao abrir", Toast.LENGTH_SHORT).show();
        }
    }

    public void fazerLigacao(String numeroTelefone) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                String numeroLimpo = numeroTelefone.replaceAll("[^0-9]", "");
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + numeroLimpo));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao fazer ligação: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Permissão para fazer ligações não concedida", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCampos() {
        EditText edtNome = findViewById(R.id.edtNome);
        EditText edtCelular = findViewById(R.id.edtCelular);
        EditText edtLatitude = findViewById(R.id.edtLatitude);
        EditText edtLongitude = findViewById(R.id.edtLongitude);

        edtNome.setText("");
        edtCelular.setText("");
        edtLatitude.setText("");
        edtLongitude.setText("");
    }

    private void esconderCadastroAmigo() {
        findViewById(R.id.include_listar_amigos).setVisibility(View.VISIBLE);
        findViewById(R.id.include_cadastrar_amigo).setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
    }

    private void esconderListaAmigos() {
        // Aqui você pode adicionar lógica adicional se necessário
        // Por exemplo, salvar estado ou limpar seleções
    }

    private void atualizarContadorAmigos() {
        DbAmigosDAO dao = new DbAmigosDAO(this);
        int totalAmigos = dao.listarAmigos().size();

        // Atualizar título da ActionBar ou TextView com o contador
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Agenda (" + totalAmigos + " amigos)");
        }
    }

    private void configurarRecycler() {
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DbAmigosDAO dao = new DbAmigosDAO(this);
        adapter = new DbAmigosAdapter(dao.listarAmigos(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean todasPermissoesConcedidas = true;
            for (int resultado : grantResults) {
                if (resultado != PackageManager.PERMISSION_GRANTED) {
                    todasPermissoesConcedidas = false;
                    break;
                }
            }

            if (!todasPermissoesConcedidas) {
                Toast.makeText(this, "Algumas permissões foram negadas. Funcionalidades podem não funcionar corretamente.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}