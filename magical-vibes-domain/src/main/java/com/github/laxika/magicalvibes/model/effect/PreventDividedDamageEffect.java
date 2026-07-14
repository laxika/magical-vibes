package com.github.laxika.magicalvibes.model.effect;

/**
 * "Prevent the next N damage that would be dealt this turn to any number of targets, divided as you
 * choose." (Remedy).
 *
 * <p>The reverse of {@link DealDividedDamageEffect} CHOSEN mode: the controller announces per-target
 * amounts (summing to {@code amount}) that ride on {@code StackEntry.damageAssignments}, and the
 * handler adds a "next X damage" prevention shield to each target (creatures and/or players). Each
 * target needs at least 1, so {@code amount} is the effective cap on the number of targets. The
 * shields expire at end of turn (cleared in turn cleanup like every prevention shield).
 */
public record PreventDividedDamageEffect(int amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
