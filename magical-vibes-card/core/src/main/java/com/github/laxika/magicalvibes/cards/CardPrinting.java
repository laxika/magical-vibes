package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;

import java.util.function.Supplier;

public record CardPrinting(
        String setCode,
        String collectorNumber,
        String cardClassName,
        String simpleCardClassName,
        boolean hasBackFace,
        Supplier<Card> factory) {

    /**
     * Builds the card and stamps the printing identity on it.
     *
     * <p>The back face is stamped here too, and only here. A front face builds its back face
     * inside its own constructor, which runs before this method assigns the front's set code and
     * collector number — so the {@code backFace.setSetCode(getSetCode())} calls the card classes
     * make at that point copy nulls. Without this, a transformed permanent reaches the client with
     * no printing identity and the client cannot fetch its art.
     */
    public Card createCard() {
        Card card = factory.get();
        card.setSetCode(setCode);
        card.setCollectorNumber(collectorNumber);
        Card backFace = card.getBackFaceCard();
        if (backFace != null) {
            backFace.setSetCode(setCode);
            backFace.setCollectorNumber(collectorNumber);
        }
        return card;
    }
}
