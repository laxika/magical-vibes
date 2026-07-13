package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the controller chooses a basic land type and each land they control
 * <em>becomes</em> that type until end of turn, losing its other land types and mana
 * ability per MTG rule 305.7 (Elsewhere Flask).
 *
 * <p>The type change is applied once, at resolution, to the lands the controller controls at
 * that moment; lands that enter later this turn are unaffected. Each affected land stores a
 * transient override (like {@link GrantBasicLandTypeToTargetEffect} with {@code replacing=true})
 * that is cleared at end of turn.
 */
public record OwnLandsBecomeChosenTypeUntilEndOfTurnEffect() implements CardEffect {
}
