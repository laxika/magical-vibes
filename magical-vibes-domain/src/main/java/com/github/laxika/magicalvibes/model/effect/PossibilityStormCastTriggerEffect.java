package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for Possibility Storm's {@code ON_ANY_PLAYER_CASTS_SPELL} trigger.
 *
 * <p>Only spells cast from hand trigger it; the free cast it grants happens from exile, so the
 * ability cannot chain into itself.</p>
 */
public record PossibilityStormCastTriggerEffect() implements CardEffect {
}
