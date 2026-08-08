package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * "Target player mills {@code count} cards. You draw a card for each {@code cardType} card put into
 * their graveyard this way." (Coerced Confession with {@link CardType#CREATURE}, Patient Rebuilding
 * with {@link CardType#LAND}.)
 * <p>
 * Only the cards this effect actually put into the target's graveyard are counted, so a milled card
 * of the right type that a replacement effect diverted elsewhere does not add a draw. Targets a
 * player.
 */
public record MillTargetPlayerAndDrawPerTypeMilledEffect(int count, CardType cardType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
