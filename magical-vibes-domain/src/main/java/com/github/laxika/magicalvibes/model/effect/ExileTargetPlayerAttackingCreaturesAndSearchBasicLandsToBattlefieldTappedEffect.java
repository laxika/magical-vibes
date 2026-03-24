package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile all attacking creatures target player controls. That player may search their library
 * for that many basic land cards, put those cards onto the battlefield tapped, then shuffle.
 *
 * <p>Targets a player ({@link #canTargetPlayer()} returns true). The number of basic lands
 * the player may search for equals the number of creatures actually exiled.
 *
 * <p>Used by Settle the Wreckage.
 */
public record ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
