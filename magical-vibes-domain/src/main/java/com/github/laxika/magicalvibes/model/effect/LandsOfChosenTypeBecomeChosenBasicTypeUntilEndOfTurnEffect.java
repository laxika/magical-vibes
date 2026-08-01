package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the controller chooses a land type and a basic land type; each land of the first
 * chosen type (any controller) <em>becomes</em> the second chosen type until end of turn, losing
 * its other land types and mana ability per MTG rule 305.7 (Vision Charm).
 *
 * <p>The type change is applied once, at resolution, to lands on the battlefield that currently
 * have the first type; lands that enter later this turn are unaffected. Each affected land stores
 * a transient override (like {@link GrantBasicLandTypeToTargetEffect} with {@code replacing=true})
 * that is cleared at end of turn.
 */
public record LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect() implements CardEffect {
}
