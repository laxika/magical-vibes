package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to target player or planeswalker.
 * Unlike {@link DealDamageToTargetOpponentOrPlaneswalkerEffect}, any player may be
 * chosen (including the controller); planeswalker permanents are also valid targets.
 * Used by Boggart Shenanigans and similar cards.
 *
 * @param damage the amount of damage to deal
 */
public record DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage) implements CardEffect {

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
