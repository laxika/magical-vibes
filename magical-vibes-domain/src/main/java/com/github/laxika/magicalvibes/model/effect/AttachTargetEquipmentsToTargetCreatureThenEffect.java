package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Attaches the targeted Equipment to the targeted creature, then queues a follow-up if any attached. */
public record AttachTargetEquipmentsToTargetCreatureThenEffect(CardEffect thenEffect,
                                                               int creatureTargetGroup,
                                                               int equipmentTargetGroup)
        implements CardEffect {

    public AttachTargetEquipmentsToTargetCreatureThenEffect(CardEffect thenEffect) {
        this(thenEffect, 0, 1);
    }

    public AttachTargetEquipmentsToTargetCreatureThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
        if (creatureTargetGroup < 0 || equipmentTargetGroup < 0) {
            throw new IllegalArgumentException("Target groups must be non-negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
