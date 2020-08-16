package org.bitcoinj.core;

import org.bitcoinj.core.slp.SlpAddress;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddressValidationTests {

    @Test
    public void validateCashAddrs() {
        String cashAddrP2PKHValid = "bitcoincash:qzhr268ppgwtr36h20apc6ahzx6lsmlmcy40qejee0";
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2PKHValid));

        String cashAddrP2PKHNoPrefixValid = "qzhr268ppgwtr36h20apc6ahzx6lsmlmcy40qejee0";
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2PKHNoPrefixValid));

        String cashAddrP2SHValid = "bitcoincash:pqeft5ukkg9ew2fgt5axxnrq853nc5cuyvvpusv2nz";
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2SHValid));

        String cashAddrP2SHNoPrefixValid = "pqeft5ukkg9ew2fgt5axxnrq853nc5cuyvvpusv2nz";
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2SHNoPrefixValid));

        String cashAddrP2PKHInvalid = "bitcoincash:qzhr268ppgwtr36h2smlmcy40qejeec";
        assertFalse(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2PKHInvalid));

        String cashAddrP2PKHNoPrefixInvalid = "qzhr268ppgwtr36xclsmlmcy40qejeec";
        assertFalse(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2PKHNoPrefixInvalid));

        String cashAddrP2SHInvalid = "bitcoincash:pqef5axxnrq853nc5cuyvvpusv2nz";
        assertFalse(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2SHInvalid));

        String cashAddrP2SHNoPrefixInvalid = "pqeft5ukkg9ew2fgt5axxncuyvvpusv2nz";
        assertFalse(Address.isValidCashAddr(MainNetParams.get(), cashAddrP2SHNoPrefixInvalid));
    }

    @Test
    public void validateLegacy() {
        String legacyP2PKHValid = "1N2LSmQCdL6Sv2B1avm6joiQCzZeeUqDyL";
        String legacyP2PKHInvalid = "1N2LSmQvdL6Sv2B1avm6joiQCzZeeUqDyL";

        assertTrue(Address.isValidLegacyAddress(MainNetParams.get(), legacyP2PKHValid));
        assertFalse(Address.isValidLegacyAddress(MainNetParams.get(), legacyP2PKHInvalid));

        String legacyToCashAddrValid = CashAddressFactory.create().getFromBase58(MainNetParams.get(), legacyP2PKHValid).toString();
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), legacyToCashAddrValid));
    }

    @Test
    public void validateSlp() {
        String slpP2PKHValid = "simpleledger:qrnfkj0ya6sju73qf2m0cn05j23d5ztlyqteejq984";
        String slpP2SHValid = "simpleledger:pphd9qsajfgwlcx706ed8d7l650f6q36c57lleqapv";
        String slpP2PKHInvalid = "simpleledger:qrnfkj0ya6sju73qf2m0cz05j23d5ztlyqteejq984";
        SlpAddress validSlpP2PKH = new SlpAddress(MainNetParams.get(), slpP2PKHValid);
        SlpAddress validSlpP2SH = new SlpAddress(MainNetParams.get(), slpP2SHValid);
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), validSlpP2PKH.toCashAddress()));
        assertTrue(Address.isValidCashAddr(MainNetParams.get(), validSlpP2SH.toCashAddress()));
        assertFalse(Address.isValidSlpAddress(MainNetParams.get(), slpP2PKHInvalid));
        assertEquals("bitcoincash:qrnfkj0ya6sju73qf2m0cn05j23d5ztlyq8zjf49et", validSlpP2PKH.toCashAddress());
        assertEquals("bitcoincash:pphd9qsajfgwlcx706ed8d7l650f6q36c5jy5z4alj", validSlpP2SH.toCashAddress());
    }

    @Test
    public void validateSlpTestnet() {
        String slpP2PKHValid = "slptest:qz0e574avqxqe2srnqa80jxrm78qvt9jlgdle7uqgt";
        String slpP2SHValid = "slptest:pp2fu0f6tmzrsw48k2n563ecfhgp7ryh3ywhg8py7v";
        SlpAddress validSlpP2PKH = new SlpAddress(TestNet3Params.get(), slpP2PKHValid);
        SlpAddress validSlpP2SH = new SlpAddress(TestNet3Params.get(), slpP2SHValid);
        assertTrue(Address.isValidSlpAddress(TestNet3Params.get(), validSlpP2PKH.toString()));
        assertTrue(Address.isValidCashAddr(TestNet3Params.get(), validSlpP2PKH.toCashAddress()));
        assertTrue(Address.isValidSlpAddress(TestNet3Params.get(), slpP2SHValid));
        assertTrue(Address.isValidCashAddr(TestNet3Params.get(), validSlpP2SH.toCashAddress()));
        assertEquals("bchtest:qz0e574avqxqe2srnqa80jxrm78qvt9jlgkt79xh6k", validSlpP2PKH.toCashAddress());
        assertEquals("bchtest:pp2fu0f6tmzrsw48k2n563ecfhgp7ryh3y4r0umnv3", validSlpP2SH.toCashAddress());
    }

    @Test
    public void testInvalidSlpP2SH() {
        String slpP2SHInvalid = "slptest:pp2fu0f6tmarsw48k2n563ecfhgp7ryh3ywhg8py7v";
        assertFalse(Address.isValidSlpAddress(TestNet3Params.get(), slpP2SHInvalid));
    }

    @Test
    public void validateLegacyTestnet() {
        String legacyP2PKHValid = "mv4rnyY3Su5gjcDNzbMLKBQkBicCtHUtFB";
        String legacyP2PKHInvalid = "mv4rnyY3vu5gjcDNzbMLKBQkBicCtHUtFB";

        assertTrue(Address.isValidLegacyAddress(TestNet3Params.get(), legacyP2PKHValid));
        assertFalse(Address.isValidLegacyAddress(TestNet3Params.get(), legacyP2PKHInvalid));

        String legacyToCashAddrValid = CashAddressFactory.create().getFromBase58(TestNet3Params.get(), legacyP2PKHValid).toString();
        assertTrue(Address.isValidCashAddr(TestNet3Params.get(), legacyToCashAddrValid));
        assertEquals("bchtest:qz0e574avqxqe2srnqa80jxrm78qvt9jlgkt79xh6k", legacyToCashAddrValid);
    }

    @Test
    public void validateSlpType() {
        String slpP2PKHValid = "simpleledger:qrnfkj0ya6sju73qf2m0cn05j23d5ztlyqteejq984";
        String slpP2SHValid = "simpleledger:pphd9qsajfgwlcx706ed8d7l650f6q36c57lleqapv";

        SlpAddress validSlpP2PKH = new SlpAddress(MainNetParams.get(), slpP2PKHValid);
        SlpAddress validSlpP2SH = new SlpAddress(MainNetParams.get(), slpP2SHValid);

        assertFalse(validSlpP2PKH.isP2SHAddress());
        assertTrue(validSlpP2SH.isP2SHAddress());
        assertEquals("bitcoincash:pphd9qsajfgwlcx706ed8d7l650f6q36c5jy5z4alj", validSlpP2SH.toCashAddress());
    }
}