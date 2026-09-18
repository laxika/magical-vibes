package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** A creature without the excluded subtype died under the effect controller's control this turn. */
public record NonSubtypeCreatureDiedUnderYourControlThisTurn(CardSubtype excludedSubtype) implements Condition {

    @Override
    public String conditionName() {
        return "a non-" + excludedSubtype.getDisplayName() + " creature died under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no non-" + excludedSubtype.getDisplayName() + " creature died under your control this turn";
    }
}
