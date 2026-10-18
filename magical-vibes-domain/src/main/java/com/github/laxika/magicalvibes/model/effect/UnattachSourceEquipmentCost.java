package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that unattaches the equipment that granted the activated ability.
 *
 * <p>The granting Equipment is identified at activation time through
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()}.</p>
 *
 * <p>By default the Equipment is the source of any damage the ability deals ("Shuriken deals 2 damage").
 * When {@code equippedCreatureIsDamageSource} is set, the wording is "this creature deals damage" and the
 * equipped creature stays the source (Surestrike Trident).</p>
 */
public record UnattachSourceEquipmentCost(boolean equippedCreatureIsDamageSource) implements CostEffect {

    public UnattachSourceEquipmentCost() {
        this(false);
    }
}
