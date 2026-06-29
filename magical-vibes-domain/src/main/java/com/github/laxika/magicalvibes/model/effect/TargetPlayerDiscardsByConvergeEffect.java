package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards X cards, where X is the number of colors of mana spent to cast this spell.
 * The Converge value is snapshotted into the stack entry's xValue at cast time.
 */
public record TargetPlayerDiscardsByConvergeEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
