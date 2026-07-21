package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from target player's hand." Every card in the targeted player's hand is exiled
 * face up; there is no player choice and no play permission. Exiling from hand is not a discard, so
 * no discard triggers fire. Used by Identity Crisis, where it is paired with an
 * {@link ExileGraveyardCardsEffect} ({@code TARGET_PLAYER_ENTIRE}) on the same target player.
 */
public record ExileTargetPlayerHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
