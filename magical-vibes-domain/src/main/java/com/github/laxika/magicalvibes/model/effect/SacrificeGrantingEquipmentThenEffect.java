package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Sacrifices the Equipment that granted this effect and, if it was sacrificed, creates a
 * reflexive triggered ability for {@code thenEffect}.
 *
 * <p>The target of the reflexive ability is chosen only after the Equipment has been sacrificed,
 * so the wrapper itself deliberately has no target.</p>
 *
 * @param thenEffect the payload of the reflexive triggered ability
 * @param grantingEquipmentId the Equipment to sacrifice, bound when the trigger is collected
 */
public record SacrificeGrantingEquipmentThenEffect(CardEffect thenEffect, UUID grantingEquipmentId)
        implements GrantingPermanentAwareEffect {

    public SacrificeGrantingEquipmentThenEffect(CardEffect thenEffect) {
        this(thenEffect, null);
    }

    public SacrificeGrantingEquipmentThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException(
                    "SacrificeGrantingEquipmentThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }

    @Override
    public CardEffect withGrantingPermanentId(UUID permanentId) {
        return new SacrificeGrantingEquipmentThenEffect(thenEffect, permanentId);
    }
}
