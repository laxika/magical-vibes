package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that exiles the Equipment granting this ability.
 * The source Equipment is identified at activation time via
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()}.
 */
public record ExileSourceEquipmentCost() implements CostEffect {
}
