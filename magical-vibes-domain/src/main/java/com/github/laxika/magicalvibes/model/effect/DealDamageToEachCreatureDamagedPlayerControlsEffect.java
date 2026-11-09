package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each creature "that player" controls: the player dealt damage, which the damage-to-a-player
 * collector records on the stack entry (its non-targeting {@code targetId}). With no fixed amount, the damage is
 * "that much", the entry's event value. Used by Balefire Dragon and Shockmaw Dragon.
 */
public record DealDamageToEachCreatureDamagedPlayerControlsEffect(Integer fixedDamage)
        implements CardEffect {

    public DealDamageToEachCreatureDamagedPlayerControlsEffect() {
        this(null);
    }

    public DealDamageToEachCreatureDamagedPlayerControlsEffect(int fixedDamage) {
        this(Integer.valueOf(fixedDamage));
    }
}
