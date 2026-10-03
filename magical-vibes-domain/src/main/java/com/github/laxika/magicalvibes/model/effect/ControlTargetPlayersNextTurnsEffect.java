package com.github.laxika.magicalvibes.model.effect;

/**
 * The first target controls the second target during the second target's next turn, and the
 * second target controls the first target during the first target's next turn (Cruel Entertainment).
 */
public record ControlTargetPlayersNextTurnsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), false, null, false, 2);
    }
}
