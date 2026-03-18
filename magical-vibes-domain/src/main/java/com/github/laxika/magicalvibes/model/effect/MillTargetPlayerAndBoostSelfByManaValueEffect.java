package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills one card from the target player's library, then gives the source creature
 * +X/+X until end of turn, where X is the milled card's mana value.
 * Used by Mindshrieker.
 */
public record MillTargetPlayerAndBoostSelfByManaValueEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
