package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power of the permanent the source Aura or Equipment is attached to. Negative values
 * are clamped to zero unless {@code allowNegative} is true, for effects that double power (Junk Jet).
 * Uses the trigger-time power when the formerly enchanted permanent has left the battlefield.
 */
public record EnchantedPermanentPower(boolean allowNegative) implements DynamicAmount {

    public EnchantedPermanentPower() {
        this(false);
    }
}
