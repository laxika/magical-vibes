package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect that changes control of the enchanted creature.
 *
 * @param controlledByMonarch when true, the current monarch controls the enchanted creature;
 *                            otherwise the Aura's controller does
 */
public record ControlEnchantedCreatureEffect(boolean controlledByMonarch) implements CardEffect {

    public ControlEnchantedCreatureEffect() {
        this(false);
    }
}
