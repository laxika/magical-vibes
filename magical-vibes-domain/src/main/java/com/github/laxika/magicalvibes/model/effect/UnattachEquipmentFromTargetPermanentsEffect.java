package com.github.laxika.magicalvibes.model.effect;

/**
 * Unattaches Equipment (CR 701.3d): "unattach all Equipment from target creature" by default (every Equipment attached
 * to the effect's targets, which it declares), or the Equipment {@code equipment} names ("unattach enchanted Equipment":
 * {@code Subjects.attached()}; "unattach it" in a delayed ability about an Equipment: {@code Subjects.triggering()}).
 * Only a permanent that is an Equipment as the effect resolves and is attached to something becomes unattached; it
 * stays on the battlefield, and its "becomes unattached" event occurs.
 *
 * @param equipment the Equipment to unattach; {@code null} for every Equipment attached to the targets
 */
public record UnattachEquipmentFromTargetPermanentsEffect(PermanentSubject equipment) implements CardEffect {

    /** "Unattach all Equipment from target creature." */
    public UnattachEquipmentFromTargetPermanentsEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (equipment == null || equipment.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        return TargetSpec.NONE;
    }
}
