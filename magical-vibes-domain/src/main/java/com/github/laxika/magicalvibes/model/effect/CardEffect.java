package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public interface CardEffect {
    default boolean canTargetPlayer() { return false; }
    default boolean canTargetPermanent() { return false; }
    default boolean canTargetSpell() { return false; }
    default boolean canTargetGraveyard() { return false; }
    default boolean canTargetAnyGraveyard() { return false; }
    default boolean canTargetExile() { return false; }

    /**
     * Returns an optional predicate that restricts which permanents can be targeted
     * by this effect. Used by saga chapter targeting to filter valid choices.
     * Returns {@code null} when no restriction applies (any valid permanent can be targeted).
     */
    default PermanentPredicate targetPredicate() { return null; }

    /**
     * Returns {@code true} if this effect implicitly targets its source permanent
     * (e.g. boost-self, animate-self, regenerate-self). Used by
     * {@code ActivatedAbilityExecutionService} to auto-assign the source as
     * the target when no explicit target is provided.
     */
    default boolean isSelfTargeting() { return false; }

    /**
     * Returns {@code true} if this effect deals damage or destroys a permanent.
     * Used by targeting validation to check protection from source.
     */
    default boolean isDamageOrDestruction() { return false; }

    /**
     * Returns {@code true} if this effect is a characteristic-defining ability
     * that sets power and/or toughness (e.g. "* / * where * is ...").
     * Used by copy effects with P/T overrides (CR 707.9d): when a copy effect
     * provides specific P/T values, CDAs that define P/T are not copied.
     */
    default boolean isPowerToughnessDefining() { return false; }
}

