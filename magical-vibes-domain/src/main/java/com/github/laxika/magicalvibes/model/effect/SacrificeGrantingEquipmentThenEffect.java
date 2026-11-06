package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the Equipment that granted the triggered ability this effect is part of, the stack entry's granting
 * permanent ({@code StackEntry.getGrantingPermanentId()}), and, if it was sacrificed, creates a reflexive triggered
 * ability for {@code thenEffect}.
 *
 * <p>The target of the reflexive ability is chosen only after the Equipment has been sacrificed,
 * so the wrapper itself deliberately has no target.</p>
 *
 * @param thenEffect the payload of the reflexive triggered ability
 */
public record SacrificeGrantingEquipmentThenEffect(CardEffect thenEffect) implements CardEffect {

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
}
