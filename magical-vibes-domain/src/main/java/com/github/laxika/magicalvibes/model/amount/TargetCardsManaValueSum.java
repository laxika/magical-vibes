package com.github.laxika.magicalvibes.model.amount;

/** The total mana value of the graveyard cards carried by the stack entry's target list. */
public record TargetCardsManaValueSum(boolean graveyardOnly) implements DynamicAmount {
    public TargetCardsManaValueSum() {
        this(false);
    }
}
