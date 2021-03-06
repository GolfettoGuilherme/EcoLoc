package br.com.fabappu9.ecoloc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.com.fabappu9.ecoloc.DTO.RankingDto;
import br.com.fabappu9.ecoloc.DTO.UsuarioDto;
import br.com.fabappu9.ecoloc.Model.RespostaLogin;
import br.com.fabappu9.ecoloc.Permissoes.Permissoes;
import br.com.fabappu9.ecoloc.data.SharedPreferenceHelper;
import br.com.fabappu9.ecoloc.network.APIClient;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private TextView user;
    private TextView pass;
    private Button cadastrar;
    private Button login;
    private SpotsDialog dialog;
    private List<RankingDto> pontosRank =null;
    Call<List<RankingDto>> retornoPonto = null;
    CheckBox checkBox;
    String Usuario;
    String Senha;
    String ponto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("EcoLoc");
        dialog = new SpotsDialog(this, R.style.estilo_carregando);
        Permissoes permissoes = new Permissoes();
        permissoes.setMyPermissionsRequestAccessFineLocation(LoginActivity.this);
        cadastrar  = (Button) findViewById(R.id.CadastrarButton);
        checkBox = (CheckBox) findViewById(R.id.check_lembre_me);
        user = (TextView) findViewById(R.id.txtLoginUser);
        pass = (TextView) findViewById(R.id.txtLoginPass);
        login  = (Button) findViewById(R.id.LoginButton);

        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(LoginActivity.this);
        if (sharedPreferenceHelper.getCheckLogin()){
            user.setText(sharedPreferenceHelper.getUsuarioLogin());
            pass.setText(sharedPreferenceHelper.getSenhaLogin());
            checkBox.setChecked(true);
            login.callOnClick();
        }
        cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(LoginActivity.this, CadastradoActivity.class);
                startActivity(intent1);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                Usuario = user.getText().toString();
                Senha = pass.getText().toString();

                Call<RespostaLogin> retorno = null;
                if (TextUtils.isEmpty(Usuario) || TextUtils.isEmpty(Senha)) {
                    Toast.makeText(LoginActivity.this, "Campo usuario ou senha em branco.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    retorno = new APIClient().getRestService().setUsuarioLoginDTO("12345", "GETLOGARUSUARIO", Usuario, Senha);
                    configurarCallback(retorno);
                }
            }
        });
    }
    private void configurarCallback(Call<RespostaLogin> retorno ) {
        retorno.enqueue(new Callback<RespostaLogin>() {
            @Override
            public void onResponse(@NonNull Call<RespostaLogin> call, @NonNull Response<RespostaLogin> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(LoginActivity.this, " Erro no servidor" , Toast.LENGTH_SHORT).show();
                }else{
                    RespostaLogin login = response.body();

                    assert login != null;
                    if (login.getRETORNO().equals("SUCESSO")){
                        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(LoginActivity.this);
                        if (isCheck()){
                            getPontos(login.getID());
                            sharedPreferenceHelper.setLogin(login.getLOGIN(),login.getSENHA(), login.getNOME(), login.getFOTO(), login.getID(),ponto);
                        }else{
                            sharedPreferenceHelper.setCheckLogin();
                        }
                        Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent1);
                        dialog.dismiss();
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this, login.getRETORNO() +" ,Verifique usuário e senha" , Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<RespostaLogin> call, @NonNull Throwable error) {
                Toast.makeText(LoginActivity.this, "Deu Ruim: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public boolean isCheck(){
        return  checkBox.isChecked();
    }

    public void getPontos(String id){
        retornoPonto = new APIClient().getRestService().getRankingDTO("12345", "GETRANKING",id);
        retornoPonto.enqueue(new Callback<List<RankingDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<RankingDto>> call, @NonNull Response<List<RankingDto>> response) {
                if (!response.isSuccessful()) {
                    Log.e("ERRO:", response.message());
                } else {
                    pontosRank = response.body();
                    for (int i = 0; i < pontosRank.size(); i++) {
                        Toast.makeText(LoginActivity.this, "Pontos " + pontosRank.get(i).getPONTUACAO(), Toast.LENGTH_SHORT).show();

                        ponto = pontosRank.get(i).getPONTUACAO();
                    }
                    SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(LoginActivity.this);
                    sharedPreferenceHelper.setPontoLogin(ponto);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<RankingDto>> call, @NonNull Throwable error) {
                Log.e("ERRO:", error.getMessage());
            }
        });
    }

}