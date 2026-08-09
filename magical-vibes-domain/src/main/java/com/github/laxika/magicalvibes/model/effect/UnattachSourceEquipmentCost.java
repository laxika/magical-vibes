package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that unattaches the equipment that granted the activated ability.
 *
 * <p>The granting Equipment is identified at activation time through
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()}.</p>
 */
public record UnattachSourceEquipmentCost() implements CostEffect {
}
