package com.github.laxika.magicalvibes.model.effect;

/**
 * You control target player during that player's next turn (Mindslaver).
 * When {@code grantExtraTurnAfter} is true (Emrakul, the Promised End), that player also takes
 * an extra turn after the controlled turn — scheduled when control actually activates so a skip
 * or loss during the controlled turn does not orphan an early-queued extra turn.
 */
public record ControlTargetPlayerNextTurnEffect(boolean grantExtraTurnAfter) implements CardEffect {

    public ControlTargetPlayerNextTurnEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
