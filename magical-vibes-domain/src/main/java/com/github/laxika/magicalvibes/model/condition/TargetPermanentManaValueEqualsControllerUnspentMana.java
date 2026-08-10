package com.github.laxika.magicalvibes.model.condition;

/** The targeted permanent's mana value equals the ability controller's unspent mana. */
public record TargetPermanentManaValueEqualsControllerUnspentMana() implements Condition {

    @Override
    public String conditionName() {
        return "target mana value equals unspent mana";
    }

    @Override
    public String conditionNotMetReason() {
        return "target mana value does not equal unspent mana";
    }
}
