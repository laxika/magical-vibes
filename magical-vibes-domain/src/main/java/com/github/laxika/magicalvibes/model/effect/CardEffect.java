package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public interface CardEffect {

    /**
     * The declarative targeting descriptor for this effect. This is the single source of truth
     * that the legacy per-effect targeting methods below derive from; migrated effects override
     * ONLY this method (deleting their legacy overrides). Unmigrated effects leave it at
     * {@link TargetSpec#NONE}, for which every derived value equals the historical default, and
     * keep their per-record legacy overrides (a per-record override always wins over these
     * interface defaults).
     */
    default TargetSpec targetSpec() { return TargetSpec.NONE; }

    default boolean canTargetPlayer() { return targetSpec().category().includesPlayers(); }
    default boolean canTargetPermanent() { return targetSpec().category().includesPermanents(); }
    default boolean canTargetSpell() { return targetSpec().category() == TargetCategory.SPELL_ON_STACK; }
    default boolean canTargetGraveyard() {
        TargetCategory category = targetSpec().category();
        return category == TargetCategory.GRAVEYARD_CARD
                || category == TargetCategory.ANY_GRAVEYARD_CARD
                || category == TargetCategory.CONTROLLERS_GRAVEYARD_CARD;
    }
    default boolean canTargetAnyGraveyard() { return targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD; }
    /**
     * When true, graveyard targeting is restricted to the controller's graveyard only.
     *
     * <p>Derived from the dedicated {@link TargetCategory#CONTROLLERS_GRAVEYARD_CARD} category.
     * The three graveyard zone-states — opponent-only ({@code GRAVEYARD_CARD}), any
     * ({@code ANY_GRAVEYARD_CARD}), and controller-only ({@code CONTROLLERS_GRAVEYARD_CARD}) —
     * form one mutually-exclusive dimension, so each maps to exactly one category and back with no
     * ambiguity (see the {@code SpellCastingService} / {@code AiTargetSelector} readers that branch
     * on this boolean and {@code canTargetAnyGraveyard}).</p>
     */
    default boolean targetsControllersGraveyardOnly() {
        return targetSpec().category() == TargetCategory.CONTROLLERS_GRAVEYARD_CARD;
    }
    default boolean canTargetExile() { return targetSpec().category() == TargetCategory.EXILE_CARD; }

    /**
     * Returns an optional predicate that restricts which permanents can be targeted
     * by this effect. Used by saga chapter targeting to filter valid choices.
     * Returns {@code null} when no restriction applies (any valid permanent can be targeted).
     */
    default PermanentPredicate targetPredicate() { return targetSpec().predicate(); }

    /**
     * Returns {@code true} if this effect implicitly targets its source permanent
     * (e.g. boost-self, animate-self, regenerate-self). Used by
     * {@code ActivatedAbilityExecutionService} to auto-assign the source as
     * the target when no explicit target is provided.
     */
    default boolean isSelfTargeting() { return targetSpec().selfTargeting(); }

    /**
     * Returns the number of distinct player targets this effect requires.
     * Default is 1 (single player target). Override for effects that target
     * multiple players (e.g. "two target players exchange life totals" returns 2).
     */
    default int requiredPlayerTargetCount() { return targetSpec().playerTargetCount(); }

    /**
     * Returns {@code true} if this effect deals damage or destroys a permanent.
     * Used by targeting validation to check protection from source.
     */
    default boolean isDamageOrDestruction() { return targetSpec().harmful(); }

    /**
     * Returns {@code true} if this effect is a characteristic-defining ability
     * that sets power and/or toughness (e.g. "* / * where * is ...").
     * Used by copy effects with P/T overrides (CR 707.9d): when a copy effect
     * provides specific P/T values, CDAs that define P/T are not copied.
     */
    default boolean isPowerToughnessDefining() { return false; }
}
