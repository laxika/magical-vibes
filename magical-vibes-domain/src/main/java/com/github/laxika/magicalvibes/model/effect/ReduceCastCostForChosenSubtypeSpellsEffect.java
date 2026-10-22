package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana cost of creature spells that have the source permanent's chosen creature
 * subtype. {@code scope} selects whose spells qualify: SELF for the source controller's spells
 * (Herald's Horn), ALL for every player's spells (Urza's Incubator).
 */
public record ReduceCastCostForChosenSubtypeSpellsEffect(int amount, CostModificationScope scope)
        implements CardEffect {

    public ReduceCastCostForChosenSubtypeSpellsEffect(int amount) {
        this(amount, CostModificationScope.SELF);
    }
}
