package org.bitcoinj.net;

import com.subgraph.orchid.encoders.Hex;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.HashHelper;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.JSONHelper;
import org.bitcoinj.wallet.SendRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NetHelper {
    private String[] cashAcctServers = new String[]{
            "https://cashacct.imaginary.cash",
            "https://cashaccounts.bchdata.cash",
            "https://cashacct.electroncash.dk"
    };

    private String[] blockExplorers = new String[]{
            "btc.com"
    };

    private String[] blockExplorerAPIURL = new String[]{
            "https://bch-chain.api.btc.com/v3/tx/"
    };

    public String getCashAccountAddress(NetworkParameters params, String cashAccount)
    {
        String[] splitAccount = cashAccount.split("#");
        String username = splitAccount[0];
        String block;

        if(cashAccount.contains("."))
        {
            String[] splitBlock = splitAccount[1].split("\\.");
            block = splitBlock[0];
        }
        else {
            block = splitAccount[1];
        }
        String txHex = getTxHexFromCashAcct(cashAccount);
        Transaction decodedTx = new Transaction(params, Hex.decode(txHex));

        String txid = decodedTx.getHashAsString();
        int txHeight = getTransactionHeight(txid);
        int blockInt = Integer.parseInt(block);
        int cashAccountGenesis = 563620;
        if(blockInt == (txHeight - cashAccountGenesis)) {
            String blockHash = getTransactionsBlockHash(txid);
            String collision = new HashHelper().getCashAccountCollision(blockHash, txid);
            String expectedAddress = getExpectedCashAccountAddress(username + "#" + block + "." + collision);
            String opReturn = getOpReturn(decodedTx);
            String hash160 = opReturn.substring(2);
            String cashAddress = CashAddressFactory.create().getFromBase58(MainNetParams.get(), new Address(params, Hex.decode(hash160)).toString()).toString();

            if(cashAddress.equals(expectedAddress))
                return cashAddress;
            else
                return "Unexpected Cash Account. Server possibly hacked.";
        } else {
            return "Unexpected Cash Account. Server possibly hacked.";
        }
    }

    public String getCashAccountAddress(NetworkParameters params, String cashAccount, Proxy proxy)
    {
        String[] splitAccount = cashAccount.split("#");
        String username = splitAccount[0];
        String block;

        if(cashAccount.contains("."))
        {
            String[] splitBlock = splitAccount[1].split("\\.");
            block = splitBlock[0];
        }
        else {
            block = splitAccount[1];
        }
        String txHex = getTxHexFromCashAcct(cashAccount, proxy);
        Transaction decodedTx = new Transaction(params, Hex.decode(txHex));

        String txid = decodedTx.getHashAsString();
        int txHeight = getTransactionHeight(txid, proxy);
        int blockInt = Integer.parseInt(block);
        int cashAccountGenesis = 563620;
        if(blockInt == (txHeight - cashAccountGenesis)) {
            String blockHash = getTransactionsBlockHash(txid, proxy);
            String collision = new HashHelper().getCashAccountCollision(blockHash, txid);
            String expectedAddress = getExpectedCashAccountAddress(username + "#" + block + "." + collision, proxy);
            String opReturn = getOpReturn(decodedTx);
            String hash160 = opReturn.substring(2);
            String cashAddress = CashAddressFactory.create().getFromBase58(MainNetParams.get(), new Address(params, Hex.decode(hash160)).toString()).toString();

            if(cashAddress.equals(expectedAddress))
                return cashAddress;
            else
                return "Unexpected Cash Account. Server possibly hacked.";
        } else {
            return "Unexpected Cash Account. Server possibly hacked.";
        }
    }

    private String getTransactionsBlockHash(String transactionHash)
    {
        int randExplorer = new Random().nextInt(blockExplorers.length);
        String blockExplorer = blockExplorers[randExplorer];
        String blockExplorerURL = blockExplorerAPIURL[randExplorer];

        String block = "";
        String txHash = transactionHash.toLowerCase();
        InputStream is = null;
        try {
            is = new URL(blockExplorerURL + txHash).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JSONHelper.readJSONFile(rd);
            JSONObject json = new JSONObject(jsonText);
            if(blockExplorer.equals("btc.com")) {
                block = json.getJSONObject("data").getString("block_hash");
            }

        } catch (JSONException e) {
            block = "???";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return block.equals("-1") ? "???" : block;
    }

    private String getTransactionsBlockHash(String transactionHash, Proxy proxy)
    {
        int randExplorer = new Random().nextInt(blockExplorers.length);
        String blockExplorer = blockExplorers[randExplorer];
        String blockExplorerURL = blockExplorerAPIURL[randExplorer];

        String block = "";
        String txHash = transactionHash.toLowerCase();
        InputStream is = null;
        try {
            is = new URL(blockExplorerURL + txHash).openConnection(proxy).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JSONHelper.readJSONFile(rd);
            JSONObject json = new JSONObject(jsonText);
            if(blockExplorer.equals("btc.com")) {
                block = json.getJSONObject("data").getString("block_hash");
            }

        } catch (JSONException e) {
            block = "???";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return block.equals("-1") ? "???" : block;
    }

    private String getTxHexFromCashAcct(String cashAccount) {
        int randExplorer = (new Random()).nextInt(cashAcctServers.length);
        String lookupServer = cashAcctServers[randExplorer];
        String txHex = "";
        String[] splitAccount = cashAccount.split("#");
        String name = splitAccount[0];
        String block = splitAccount[1];

        if(!lookupServer.contains("rest.bitcoin.com"))
        {
            if (!block.contains(".")) {
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/lookup/" + block + "/" + name)).openStream();
                } catch (IOException var56) {
                    var56.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    txHex = json.getJSONArray("results").getJSONObject(0).getString("transaction");
                } catch (JSONException | IOException var53) {
                    var53.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var47) {
                        var47.printStackTrace();
                    }

                }
            } else {
                String[] splitBlock = block.split("\\.");
                String mainBlock = splitBlock[0];
                String collision = splitBlock[1];
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/lookup/" + mainBlock + "/" + name + "/" + collision)).openStream();
                } catch (IOException var52) {
                    var52.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    txHex = json.getJSONArray("results").getJSONObject(0).getString("transaction");
                } catch (JSONException | IOException var49) {
                    var49.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var48) {
                        var48.printStackTrace();
                    }

                }
            }
        }

        return txHex;
    }

    private String getTxHexFromCashAcct(String cashAccount, Proxy proxy) {
        int randExplorer = (new Random()).nextInt(cashAcctServers.length);
        String lookupServer = cashAcctServers[randExplorer];
        String txHex = "";
        String[] splitAccount = cashAccount.split("#");
        String name = splitAccount[0];
        String block = splitAccount[1];

        if(!lookupServer.contains("rest.bitcoin.com"))
        {
            if (!block.contains(".")) {
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/lookup/" + block + "/" + name)).openConnection(proxy).getInputStream();
                } catch (IOException var56) {
                    var56.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    txHex = json.getJSONArray("results").getJSONObject(0).getString("transaction");
                } catch (JSONException | IOException var53) {
                    var53.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var47) {
                        var47.printStackTrace();
                    }

                }
            } else {
                String[] splitBlock = block.split("\\.");
                String mainBlock = splitBlock[0];
                String collision = splitBlock[1];
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/lookup/" + mainBlock + "/" + name + "/" + collision)).openConnection(proxy).getInputStream();
                } catch (IOException var52) {
                    var52.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    txHex = json.getJSONArray("results").getJSONObject(0).getString("transaction");
                } catch (JSONException | IOException var49) {
                    var49.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var48) {
                        var48.printStackTrace();
                    }

                }
            }
        }

        return txHex;
    }

    private String getOpReturn(Transaction tx)
    {
        List<TransactionOutput> outputs = tx.getOutputs();
        String opReturn = null;
        for (TransactionOutput output : outputs) {
            boolean isOpReturn = output.getScriptPubKey().isOpReturn();

            if (isOpReturn) {
                opReturn = new String(Hex.encode(Objects.requireNonNull(output.getScriptPubKey().getChunks().get(3).data)), StandardCharsets.UTF_8);
                break;
            }
        }

        return opReturn;
    }

    private int getTransactionHeight(String transactionHash)
    {
        int randExplorer = new Random().nextInt(blockExplorers.length);
        String blockExplorer = blockExplorers[randExplorer];
        String blockExplorerURL = blockExplorerAPIURL[randExplorer];

        int height = 0;
        String txHash = transactionHash.toLowerCase();
        InputStream is = null;
        try {
            is = new URL(blockExplorerURL + txHash).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JSONHelper.readJSONFile(rd);
            JSONObject json = new JSONObject(jsonText);
            if(blockExplorer.equals("btc.com")) {
                height = json.getJSONObject("data").getInt("block_height");
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return height;
    }

    private int getTransactionHeight(String transactionHash, Proxy proxy)
    {
        int randExplorer = new Random().nextInt(blockExplorers.length);
        String blockExplorer = blockExplorers[randExplorer];
        String blockExplorerURL = blockExplorerAPIURL[randExplorer];

        int height = 0;
        String txHash = transactionHash.toLowerCase();
        InputStream is = null;
        try {
            is = new URL(blockExplorerURL + txHash).openConnection(proxy).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JSONHelper.readJSONFile(rd);
            JSONObject json = new JSONObject(jsonText);
            if(blockExplorer.equals("btc.com")) {
                height = json.getJSONObject("data").getInt("block_height");
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return height;
    }

    private String getExpectedCashAccountAddress(String cashAccount) {
        int randExplorer = (new Random()).nextInt(cashAcctServers.length);
        String lookupServer = cashAcctServers[randExplorer];
        String address = "";
        String[] splitAccount = cashAccount.split("#");
        String name = splitAccount[0];
        String block = splitAccount[1];

        if(!lookupServer.contains("rest.bitcoin.com")) {
            if (!block.contains(".")) {
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/account/" + block + "/" + name)).openStream();
                } catch (IOException var56) {
                    var56.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    address = json.getJSONObject("information").getJSONArray("payment").getJSONObject(0).getString("address");
                } catch (JSONException | IOException var53) {
                    var53.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var48) {
                        var48.printStackTrace();
                    }

                }
            } else {
                String[] splitBlock = block.split("\\.");
                String mainBlock = splitBlock[0];
                String collision = splitBlock[1];
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/account/" + mainBlock + "/" + name + "/" + collision)).openStream();
                } catch (IOException var52) {
                    var52.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    address = json.getJSONObject("information").getJSONArray("payment").getJSONObject(0).getString("address");
                } catch (JSONException | IOException var49) {
                    var49.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var47) {
                        var47.printStackTrace();
                    }

                }
            }
        }

        return address;
    }

    private String getExpectedCashAccountAddress(String cashAccount, Proxy proxy) {
        int randExplorer = (new Random()).nextInt(cashAcctServers.length);
        String lookupServer = cashAcctServers[randExplorer];
        String address = "";
        String[] splitAccount = cashAccount.split("#");
        String name = splitAccount[0];
        String block = splitAccount[1];

        if(!lookupServer.contains("rest.bitcoin.com")) {
            if (!block.contains(".")) {
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/account/" + block + "/" + name)).openConnection(proxy).getInputStream();
                } catch (IOException var56) {
                    var56.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    address = json.getJSONObject("information").getJSONArray("payment").getJSONObject(0).getString("address");
                } catch (JSONException | IOException var53) {
                    var53.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var48) {
                        var48.printStackTrace();
                    }

                }
            } else {
                String[] splitBlock = block.split("\\.");
                String mainBlock = splitBlock[0];
                String collision = splitBlock[1];
                InputStream is = null;

                try {
                    is = (new URL(lookupServer + "/account/" + mainBlock + "/" + name + "/" + collision)).openConnection(proxy).getInputStream();
                } catch (IOException var52) {
                    var52.printStackTrace();
                }

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = org.bitcoinj.utils.JSONHelper.readJSONFile(rd);
                    JSONObject json = new JSONObject(jsonText);
                    address = json.getJSONObject("information").getJSONArray("payment").getJSONObject(0).getString("address");
                } catch (JSONException | IOException var49) {
                    var49.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException var47) {
                        var47.printStackTrace();
                    }

                }
            }
        }

        return address;
    }
}
