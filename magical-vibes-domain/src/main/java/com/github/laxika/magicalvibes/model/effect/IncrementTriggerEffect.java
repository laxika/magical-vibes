package com.github.laxika.magicalvibes.model.effect;

/**
 * Increment (CR keyword, Secrets of Strixhaven): "Whenever you cast a spell, if the amount of mana
 * you spent is greater than this creature's power or toughness, put a +1/+1 counter on this creature."
 * <p>
 * Placed on the {@code ON_CONTROLLER_CASTS_SPELL} slot. The spell-cast trigger collector snapshots
 * the mana spent on the casting spell into the stack entry's {@code xValue} and performs the
 * intervening-if check at trigger time; the resolution handler re-checks the condition against the
 * creature's current power/toughness (CR 603.4) before placing the counter.
 */
public record IncrementTriggerEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() {
        return true;
    }
}
