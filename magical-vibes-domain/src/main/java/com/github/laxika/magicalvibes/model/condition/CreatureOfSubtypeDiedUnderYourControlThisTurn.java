package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** A creature with the given effective subtype died under the effect controller's control this turn. */
public record CreatureOfSubtypeDiedUnderYourControlThisTurn(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "a " + subtype.getDisplayName() + " creature died under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no " + subtype.getDisplayName() + " creature died under your control this turn";
    }
}
