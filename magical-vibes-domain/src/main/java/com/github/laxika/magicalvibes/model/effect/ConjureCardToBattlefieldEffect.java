package com.github.laxika.magicalvibes.model.effect;

/** Conjures a full, non-token card by printing or by name onto the controller's battlefield. */
public record ConjureCardToBattlefieldEffect(String setCode, String collectorNumber, String cardName)
        implements CardEffect {

    public ConjureCardToBattlefieldEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, null);
    }

    public ConjureCardToBattlefieldEffect(String cardName) {
        this(null, null, cardName);
    }
}
