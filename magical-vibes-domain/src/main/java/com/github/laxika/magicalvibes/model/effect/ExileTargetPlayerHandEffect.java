package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from target player's hand." Every card in the targeted player's hand is exiled
 * face up; there is no player choice and no play permission. Exiling from hand is not a discard, so
 * no discard triggers fire. When {@code trackWithSource} is true, the cards are also associated with
 * the source permanent for later effects that refer to cards exiled with it. Used by Identity
 * Crisis, where it is paired with an {@link ExileGraveyardCardsEffect} ({@code TARGET_PLAYER_ENTIRE})
 * on the same target player, and by Hypnox.
 */
public record ExileTargetPlayerHandEffect(boolean trackWithSource) implements CardEffect {

    public ExileTargetPlayerHandEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
