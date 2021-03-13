package org.bitcoinj.core.slp.nft;

import org.bitcoinj.core.slp.SlpToken;

public class NonFungibleSlpToken extends SlpToken {
    private String nftParentId;

    public NonFungibleSlpToken(String tokenId, String nftParentId, String ticker, int decimals) {
        super(tokenId, ticker, decimals);
        this.nftParentId = nftParentId;
    }

    public String getNftParentId() {
        return this.nftParentId;
    }

    @Override
    public String toString() {
        return "SLP TOKEN [" +
                this.getTokenId() + ", " +
                this.getTicker() + ", " +
                this.getDecimals() + "" +
                "]";
    }
}
