package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player chooses a card name, then reveals the top card of their library. If that card has
 * the chosen name, that player puts it into their hand. Otherwise, they put it into their graveyard
 * and the source deals 2 damage to them. Used by Vexing Arcanix.
 */
public record VexingArcanixEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
