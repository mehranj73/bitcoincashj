package org.bitcoinj.core.slp.nft;

public class NonFungibleSlpToken {
    private String tokenId;
    private String nftParentId;
    private String ticker;
    private int decimals;

    public NonFungibleSlpToken(String tokenId, String nftParentId, String ticker, int decimals) {
        this.tokenId = tokenId;
        this.nftParentId = nftParentId;
        this.ticker = ticker;
        this.decimals = decimals;
    }

    public String getTokenId() {
        return this.tokenId;
    }

    public String getNftParentId() {
        return this.nftParentId;
    }

    public String getTicker() {
        return this.ticker;
    }

    public int getDecimals() {
        return this.decimals;
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
