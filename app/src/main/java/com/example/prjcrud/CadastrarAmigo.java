package com.example.prjcrud;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;

public class CadastrarAmigo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastrar_amigo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CadastrarAmigo.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edtNome = findViewById(R.id.edtNome);
                EditText edtCelular = findViewById(R.id.edtCelular);
                EditText edtLatitude = findViewById(R.id.edtLatitude);
                EditText edtLongitude = findViewById(R.id.edtLongitude);

                String nome = edtNome.getText().toString().trim();
                String celular = edtCelular.getText().toString().trim();
                String latitude = edtLatitude.getText().toString().trim();
                String longitude = edtLongitude.getText().toString().trim();

                if (nome.isEmpty() || celular.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                    return;
                }

                DbAmigosDAO dao = new DbAmigosDAO(getBaseContext());
                boolean sucesso = dao.salvar(nome, celular, latitude, longitude, 10);

                if (sucesso) {
                    Toast.makeText(getApplicationContext(), "Amigo salvo com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao salvar amigo!", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(CadastrarAmigo.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}