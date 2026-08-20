package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles cards from a library, then lets the controller cast any number of the exiled spells
 * without paying their mana costs. The cast choices are made during resolution; uncast cards
 * remain exiled.
 */
public record ExileTopCardsAndMayCastSpellsEffect(
        int count,
        DynamicAmount dynamicCount,
        LibraryScope scope,
        boolean trackWithSource,
        DynamicAmount manaValueLimit
) implements CombatDamageTriggerContextEffect, CombatDamageAmountAwareEffect {

    /** Exiles the top {@code count} cards of the controller's library. */
    public ExileTopCardsAndMayCastSpellsEffect(int count) {
        this(count, null, LibraryScope.CONTROLLER, false, null);
    }

    /** Exiles cards from a combat-damaged opponent's library and tracks them with the source. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, LibraryScope scope,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit) {
        this(0, dynamicCount, scope, trackWithSource, manaValueLimit);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return trackWithSource && scope == LibraryScope.TARGET_OPPONENT
                ? TriggerContext.DAMAGED_PLAYER : null;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return dynamicCount != null ? dynamicCount : manaValueLimit;
    }
}
