package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to target opponent or planeswalker.
 * Can target a player (opponent only, not self) or a planeswalker permanent.
 * Used by Burning Sun's Avatar and similar cards.
 *
 * @param damage the amount of damage to deal
 */
public record DealDamageToTargetOpponentOrPlaneswalkerEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
