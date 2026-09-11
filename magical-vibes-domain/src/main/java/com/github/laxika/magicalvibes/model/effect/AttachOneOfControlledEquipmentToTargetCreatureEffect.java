package com.github.laxika.magicalvibes.model.effect;

/** Chooses one Equipment controlled by the effect's controller and attaches it to the target creature. */
public record AttachOneOfControlledEquipmentToTargetCreatureEffect(boolean unattachAtNextEndStep)
        implements CardEffect {

    /** Uses the delayed-unattach behavior needed by effects such as Unexpected Request. */
    public AttachOneOfControlledEquipmentToTargetCreatureEffect() {
        this(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
