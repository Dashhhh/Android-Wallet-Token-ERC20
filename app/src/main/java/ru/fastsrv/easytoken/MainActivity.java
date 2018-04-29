package ru.fastsrv.easytoken;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigInteger;

import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 *
 * @author Dmitry Markelov
 * Telegram group: https://t.me/joinchat/D62dXAwO6kkm8hjlJTR9VA
 *
 * Если есть вопросы, отвечу в телеграме
 * If you have any questions, I will answer the telegram
 *
 *    Russian:
 *    Пример включает следующие функции:
 *       - Получаем адрес кошелька
 *       - Получаем баланс Eth
 *       - Получаем баланс Токена
 *       - Получаем название Токена
 *       - Получаем символ Токена
 *       - Получаем адрес Контракта Токена
 *       - Получаем общее количество выпущеных Токенов
 *
 *
 *   English:
 *   The example includes the following functions:
 *       - Get address wallet
 *       - Get balance Eth
 *       - Get balance Token
 *       - Get Name Token
 *       - Get Symbol Token
 *       - Get contract Token address
 *       - Get supply Token
 *
 */

public class MainActivity extends AppCompatActivity {

    WalletCreate wc = new WalletCreate();

    String url = config.addressethnode();

    Web3j web3 = Web3jFactory.build(new HttpService(url));

    String smartcontract = config.addresssmartcontract();
    String passwordwallet = config.passwordwallet();

    File DataDir;

    TextView ethaddress, ethbalance, tokenname, tokensymbol, tokensupply, tokenaddress, tokenbalance, tokensymbolbalance;
    EditText sendtoaddress, sendtokenvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ethaddress = (TextView) findViewById(R.id.ethaddress); // Your Ether Address
        ethbalance = (TextView) findViewById(R.id.ethbalance); // Your Ether Balance

        tokenname = (TextView) findViewById(R.id.tokenname); // Token Name
        tokensymbol = (TextView) findViewById(R.id.tokensymbol); // Token Symbol
        tokensupply = (TextView) findViewById(R.id.tokensupply); // Token Supply
        tokenaddress = (TextView) findViewById(R.id.tokenaddress); // Token Address
        tokenbalance = (TextView) findViewById(R.id.tokenbalance); // Token Balance
        tokensymbolbalance = (TextView) findViewById(R.id.tokensymbolbalance);

        sendtoaddress = (EditText) findViewById(R.id.sendtoaddress); // Address for sending ether or token
        sendtokenvalue = (EditText) findViewById(R.id.SendTokenValue); // Ammount coin for sending

        /**
         * Получаем полный путь к каталогу с ключами
         * Get the full path to the directory with the keys
         */
        DataDir = this.getExternalFilesDir("/keys/");
        File KeyDir = new File(this.DataDir.getAbsolutePath());

        /**
         * Проверяем есть ли кошельки
         * Check whether there are purses
         */
        File[] listfiles = KeyDir.listFiles();
        if (listfiles.length == 0 ) {
            /**
             * Если в директории файла кошелька, добавляем кошелек
             * If the directory file of the wallet, add the wallet
             */
            try {
                String fileName = WalletUtils.generateNewWalletFile(passwordwallet, DataDir, false);

                System.out.println("FileName: " + DataDir.toString() + fileName);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            /**
             * Если кошелек создан, начинаем выполнение потока
             * If the wallet is created, start the thread
             */
            wc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    ///////// On Click ////////////
    /**
     * Начать выполнение потока для отправки эфира или Токена
     * Start executing thread for sending Ether or sending Token
     */
    public void onClick(View view) {
        SendingCoin sc = new SendingCoin();
        switch (view.getId()) {
            case R.id.SendEther:

                break;
            case R.id.SendToken:
                sc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }

    }
    //////// end on click /////////


    ///////////////////// Create and Load Wallet /////////////////
    public class WalletCreate extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            /**
            // Получаем список файлов в каталоге
            // Get list files in folder
            */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));

            try {
                /**
                // Загружаем файл кошелька и получаем адрес
                // Upload the wallet file and get the address
                */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                // Получаем Баланс
                // Get balance Ethereum
                */
                EthGetBalance etherbalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalance);

                /**
                // Загружаем Токен
                // Download Token
                */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GAS_PRICE, GAS_LIMIT);

                /**
                // Получаем название токена
                // Get the name of the token
                */
                String tokenname = token.name().send();
                System.out.println("Token Name: " + tokenname);

                /**
                // Получаем Символ Токена
                // Get Symbol marking token
                */
                String tokensymbol = token.symbol().send();
                System.out.println("Symbol Token: " + tokensymbol);

                /**
                // Получаем адрес Токена
                // Get The Address Token
                */
                String tokenaddress = token.getContractAddress();
                System.out.println("Address Token: " + tokenaddress);

                /**
                // Получаем общее количество выпускаемых токенов
                // Get the total amount of issued tokens
                */
                BigInteger totalSupply = token.totalSupply().send();
                System.out.println("Supply Token: "+totalSupply.toString());

                /**
                // Получаем количество токенов в кошельке
                // Receive the Balance of Tokens in the wallet
                */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());

                JSONObject result = new JSONObject();
                result.put("ethaddress",address);
                result.put("ethbalance", ethbalance);
                result.put("tokenbalance", tokenbalance.toString());
                result.put("tokenname", tokenname);
                result.put("tokensymbol", tokensymbol);
                result.put("tokenaddress",tokenaddress);
                result.put("tokensupply", totalSupply.toString());
                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            ethaddress.setText(result.get("ethaddress").toString());
            ethbalance.setText(result.get("ethbalance").toString());
            tokenname.setText(result.get("tokenname").toString());
            tokensymbol.setText(result.get("tokensymbol").toString());
            tokensupply.setText(result.get("tokensupply").toString());
            tokenaddress.setText(result.get("tokenaddress").toString());
            tokenbalance.setText(result.get("tokenbalance").toString());
            tokensymbolbalance.setText(" "+result.get("tokensymbol").toString());

        }
    }
    ////////////////// End create and load wallet ////////////////


    ///////////////////// Sending Tokens /////////////////
    public class SendingCoin extends AsyncTask<Void, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));

            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                System.out.println("Eth Address: " + address);

                /**
                 * Загружаем Токен
                 * Load Token
                 */
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GAS_PRICE, GAS_LIMIT);

                String status = null;
                String balance = null;

                /**
                 * Конвертируем сумму токенов в BigInteger и отправляем на указанные адрес
                 * Convert the amount of tokens to BigInteger and send to the specified address
                 */
                BigInteger sendvalue = BigInteger.valueOf(Long.parseLong(String.valueOf(sendtokenvalue.getText())));
                status = token.transfer(String.valueOf(sendtoaddress.getText()), sendvalue).send().getTransactionHash();

                /**
                 * Обновляем баланс Токенов
                 * Renew Token balance
                 */
                BigInteger tokenbalance = token.balanceOf(address).send();
                System.out.println("Balance Token: "+ tokenbalance.toString());
                balance = tokenbalance.toString();

                /**
                 * Возвращаем из потока, Статус транзакции и баланс Токенов
                 * Returned from thread, transaction Status and Token balance
                 */
                JSONObject result = new JSONObject();
                result.put("status",status);
                result.put("balance",balance);

                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null) {
                tokenbalance.setText(result.get("balance").toString());
                Toast toast = Toast.makeText(getApplicationContext(),result.get("status").toString(), Toast.LENGTH_LONG);
                toast.show();
            } else {System.out.println();}
        }
    }
    ////////////////// End Sending Tokens ////////////////

}
