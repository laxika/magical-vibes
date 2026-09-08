package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a self-targeting effect on an ability of an Aura or Equipment that must
 * resolve against the <em>attached</em> permanent instead of the source itself ("Regenerate
 * enchanted creature", "Put two +1/+1 counters on enchanted creature").
 *
 * <p>Trigger and ability collectors capture {@code Permanent.getAttachedTo()} onto the
 * stack entry <em>before</em> costs are paid, so the attached permanent survives a cost that
 * detaches the source — sacrificing it (Carapace) or returning it to hand (Krasis Incubation).
 * That is CR 608.2h last-known information: the ability still affects the creature the source had
 * been attached to when it was activated.
 *
 * <p>Only honoured together with a self-targeting {@link TargetSpec}; an effect whose spec is
 * {@code NONE} or which declares a real target is unaffected.
 */
public interface AttachedPermanentSelfTargetingEffect extends CardEffect {
}
