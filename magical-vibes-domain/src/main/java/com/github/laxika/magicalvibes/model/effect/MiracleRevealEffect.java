package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for the draw-time "you may reveal this card for its miracle ability" choice
 * (CR 702.94a). Queued as a {@code PendingMayAbility} from {@code DrawService} when the
 * drawn card has a {@code MiracleCast} option and is the first card drawn this turn.
 */
public record MiracleRevealEffect() implements CardEffect {
}
