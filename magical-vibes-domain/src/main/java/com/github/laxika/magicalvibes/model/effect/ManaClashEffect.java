package com.github.laxika.magicalvibes.model.effect;

/**
 * Mana Clash: "You and target opponent each flip a coin. Mana Clash deals 1 damage to each player
 * whose coin comes up tails. Repeat this process until both players' coins come up heads on the
 * same flip."
 *
 * <p>Resolves as a self-contained coin-flip loop against the targeted opponent (stored in the
 * stack entry's {@code targetId}); the controller is the other participant.
 */
public record ManaClashEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
