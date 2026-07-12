package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player chooses a card name, then reveals the top card of their library. If that card has
 * the chosen name, that player puts it into their hand. Otherwise, they put it into their graveyard
 * and the source deals {@code damageOnMiss} damage to them ({@code 0} for no damage).
 * Used by Vexing Arcanix ({@code damageOnMiss = 2}).
 *
 * @param damageOnMiss damage the source deals to the player when the revealed card doesn't match
 */
public record TargetPlayerNameCardRevealTopEffect(int damageOnMiss) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
