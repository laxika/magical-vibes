package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * Exiles cards from a library, then lets the controller cast any number of the exiled spells
 * without paying their mana costs. The cast choices are made during resolution; uncast cards
 * normally remain exiled, or return to the owner's library bottom in random order when requested.
 * They may alternatively be put into their owners' hands after the cast choices are complete.
 */
public record ExileTopCardsAndMayCastSpellsEffect(
        int count,
        DynamicAmount dynamicCount,
        LibraryScope scope,
        boolean trackWithSource,
        boolean faceDown,
        DynamicAmount manaValueLimit,
        CardPredicate castFilter,
        int maxCastCount,
        boolean targetedOpponent,
        boolean putUncastCardsOnBottomRandom,
        boolean putUncastCardsIntoHand
) implements CombatDamageTriggerContextEffect, CombatDamageAmountAwareEffect {

    /** Exiles the top {@code count} cards of the controller's library. */
    public ExileTopCardsAndMayCastSpellsEffect(int count) {
        this(count, null, LibraryScope.CONTROLLER, false, false, null, null, Integer.MAX_VALUE, false, false, false);
    }

    /** Exiles cards from a combat-damaged opponent's library and tracks them with the source. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, LibraryScope scope,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit) {
        this(0, dynamicCount, scope, trackWithSource, false, manaValueLimit, null,
                Integer.MAX_VALUE, false, false, false);
    }

    /** Exiles cards and offers only cards matching {@code castFilter} for free casting. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, LibraryScope scope,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit,
                                               CardPredicate castFilter) {
        this(0, dynamicCount, scope, trackWithSource, false, manaValueLimit, castFilter,
                Integer.MAX_VALUE, false, false, false);
    }

    /** Exiles the controller's top cards and caps the free-cast offer. */
    public ExileTopCardsAndMayCastSpellsEffect(int count, DynamicAmount manaValueLimit,
                                               CardPredicate castFilter, int maxCastCount,
                                               boolean putUncastCardsOnBottomRandom) {
        this(count, null, LibraryScope.CONTROLLER, false, false, manaValueLimit, castFilter,
                maxCastCount, false, putUncastCardsOnBottomRandom, false);
    }

    /** Exiles the controller's top cards face down with this source and offers matching spells. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerTrackedFaceDown(
            int count, CardPredicate castFilter, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, LibraryScope.CONTROLLER, true, true, null, castFilter,
                maxCastCount, false, false, false);
    }

    /** Exiles a fixed number from a targeted opponent and caps the number of free casts. */
    public static ExileTopCardsAndMayCastSpellsEffect targetedOpponent(int count, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, LibraryScope.TARGET_OPPONENT, true, false, null, null,
                maxCastCount, true, false, false);
    }

    /** Exiles cards from the controller's library and randomly bottoms every card not cast. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithRandomBottom(
            int count, DynamicAmount manaValueLimit, CardPredicate castFilter, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, LibraryScope.CONTROLLER, false, false, manaValueLimit, castFilter,
                maxCastCount, false, true, false);
    }

    /** Exiles the controller's top cards and puts every card not cast into its owner's hand. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithRestToHand(int count) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, LibraryScope.CONTROLLER, false, false, null, null,
                Integer.MAX_VALUE, false, false, true);
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
