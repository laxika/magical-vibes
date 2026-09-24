package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each player and each creature that player controls equal to that player's
 * creature count. The amount is evaluated separately for each player.
 */
public record DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect()
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
