package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that taps the Equipment granting an activated ability.
 *
 * <p>The ability is activated by the equipped creature, so the normal tap cost handles that
 * creature while this cost uses the ability's granting-permanent link to tap the Equipment.</p>
 */
public record TapGrantingEquipmentCost() implements CostEffect {

    @Override
    public boolean tapsGrantingEquipment() {
        return true;
    }
}
