package com.github.laxika.magicalvibes.model.effect;

/**
 * Unattach an Equipment attached to the source permanent as an activated-ability cost.
 * The selected Equipment's mana value becomes the ability's X value.
 */
public record UnattachEquipmentFromSourceCost() implements CostEffect {

    @Override
    public boolean derivesXValueFromPayment() {
        return true;
    }

    @Override
    public boolean allowsOpponentControlledPermanentChoice() {
        return true;
    }

    @Override
    public PermanentChoiceKind permanentChoiceKind() {
        return PermanentChoiceKind.UNATTACH_EQUIPMENT_FROM_SOURCE;
    }
}
