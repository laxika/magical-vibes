package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

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
        DynamicAmount manaValueLimit,
        CardPredicate castFilter,
        int maxCastCount,
        boolean targetedOpponent
) implements CombatDamageTriggerContextEffect, CombatDamageAmountAwareEffect {

    /** Exiles the top {@code count} cards of the controller's library. */
    public ExileTopCardsAndMayCastSpellsEffect(int count) {
        this(count, null, LibraryScope.CONTROLLER, false, null, null, Integer.MAX_VALUE, false);
    }

    /** Exiles cards from a combat-damaged opponent's library and tracks them with the source. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, LibraryScope scope,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit) {
        this(0, dynamicCount, scope, trackWithSource, manaValueLimit, null,
                Integer.MAX_VALUE, false);
    }

    /** Exiles cards and offers only cards matching {@code castFilter} for free casting. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, LibraryScope scope,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit,
                                               CardPredicate castFilter) {
        this(0, dynamicCount, scope, trackWithSource, manaValueLimit, castFilter,
                Integer.MAX_VALUE, false);
    }

    /** Exiles a fixed number from a targeted opponent and caps the number of free casts. */
    public static ExileTopCardsAndMayCastSpellsEffect targetedOpponent(int count, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, LibraryScope.TARGET_OPPONENT, true, null, null, maxCastCount, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetedOpponent
                ? TargetSpec.harmful(TargetPredicates.players(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT)))
                : TargetSpec.NONE;
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
