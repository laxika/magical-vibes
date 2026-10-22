package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power of the permanent the source Aura or Equipment is attached to. Negative values
 * are clamped to zero unless {@code allowNegative} is true, for effects that double power (Junk Jet).
 * Once the formerly enchanted permanent has left the battlefield, uses the power it had just before it
 * left (last known information, CR 608.2h), falling back to the trigger-time power.
 */
public record EnchantedPermanentPower(boolean allowNegative) implements DynamicAmount {

    public EnchantedPermanentPower() {
        this(false);
    }
}
