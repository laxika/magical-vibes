package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player mills {@code count} cards. You draw a card for each creature card put into their
 * graveyard this way." (Coerced Confession.)
 * <p>
 * Only the cards this effect actually put into the target's graveyard are counted, so a milled
 * creature card that a replacement effect diverted elsewhere does not add a draw. Targets a player.
 */
public record MillTargetPlayerAndDrawPerCreatureMilledEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
