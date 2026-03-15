package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that sacrifices the equipment granting this ability (not the equipped creature).
 * Used for equipment-granted abilities like Blazing Torch's "{T}, Sacrifice Blazing Torch: ..."
 * where the equipped creature activates the ability but the equipment is sacrificed as the cost.
 *
 * <p>The source equipment is identified at activation time via
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()},
 * which is set by the static bonus system when the ability is granted.</p>
 */
public record SacrificeSourceEquipmentCost() implements CostEffect {
}
