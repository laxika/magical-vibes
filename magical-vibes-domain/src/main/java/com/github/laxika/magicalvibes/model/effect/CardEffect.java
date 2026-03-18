package com.github.laxika.magicalvibes.model.effect;

public interface CardEffect {
    default boolean canTargetPlayer() { return false; }
    default boolean canTargetPermanent() { return false; }
    default boolean canTargetSpell() { return false; }
    default boolean canTargetGraveyard() { return false; }
    default boolean canTargetAnyGraveyard() { return false; }
    default boolean canTargetExile() { return false; }

    /**
     * Returns {@code true} if this effect implicitly targets its source permanent
     * (e.g. boost-self, animate-self, regenerate-self). Used by
     * {@code ActivatedAbilityExecutionService} to auto-assign the source as
     * the target when no explicit target is provided.
     */
    default boolean isSelfTargeting() { return false; }

    /**
     * Returns {@code true} if this effect is a characteristic-defining ability
     * that sets power and/or toughness (e.g. "* / * where * is ...").
     * Used by copy effects with P/T overrides (CR 707.9d): when a copy effect
     * provides specific P/T values, CDAs that define P/T are not copied.
     */
    default boolean isPowerToughnessDefining() { return false; }
}

