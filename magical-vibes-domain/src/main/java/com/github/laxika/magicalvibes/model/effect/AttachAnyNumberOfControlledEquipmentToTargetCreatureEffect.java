package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Attach any number of Equipment you control to" a creature you control: the controller chooses them as the effect
 * resolves.
 *
 * @param subject {@code target()}, the declared target creature, or the creature an earlier step chose ("choose a
 *                Dwarf you control. Attach any number of Equipment you control to it", Armed and Armored)
 */
public record AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect(PermanentSubject subject)
        implements CardEffect {

    public AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect {
        Objects.requireNonNull(subject, "subject");
        if (!(subject instanceof PermanentSubject.Target) && subject.declaresTarget()) {
            throw new IllegalArgumentException("The Equipment go to the target or to no target: " + subject);
        }
    }

    /** "... to target creature you control". */
    public AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
