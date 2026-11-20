package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Attaches an Equipment ({@link #equipment()}) to a permanent ({@link #host()}). The default is "attach this Equipment
 * to target creature [you control]": {@code (source(), target())}. The other shapes:
 * <ul>
 *   <li>{@code (source(), triggering())}: "attach it to that creature" ({@link #forTriggeringCreature()});</li>
 *   <li>{@code (target(g), target(g'))}: "attach target Equipment to target creature" (the two target groups);</li>
 *   <li>{@code (target(), source())}: "attach target Equipment you control to this creature";</li>
 *   <li>{@code (target(), triggeringOrSource())}: "attach any number of target Equipment to it" after "whenever this
 *       or another creature enters / attacks" (every targeted Equipment);</li>
 *   <li>{@code (triggering(), target())}: "attach that Equipment to target creature" after "whenever an Equipment
 *       enters".</li>
 * </ul>
 * An Equipment that can't be attached to the host, or is already attached to it, doesn't move (CR 301.5c, 701.3b);
 * one that moves gets a new timestamp (CR 613.7e). An optional continuation is queued as a reflexive ability once the
 * Equipment is attached to the host.
 *
 * @param targetPredicate the target restriction: of the host target ("target creature you control"), or, when only
 *                        the Equipment is targeted, of the Equipment target
 */
public record AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                          boolean thenEffectOptionalTarget,
                                                          PermanentPredicate targetPredicate,
                                                          PermanentSubject equipment,
                                                          PermanentSubject host)
        implements CardEffect {

    public AttachSourceEquipmentToTargetCreatureEffect {
        equipment = equipment == null ? Subjects.source() : equipment;
        host = host == null ? Subjects.target() : host;
        if (!isNamedOne(equipment) || !isNamedOne(host)) {
            throw new IllegalArgumentException("An attach effect names one Equipment subject and one host subject: "
                    + equipment + ", " + host);
        }
        if (equipment.declaresTarget() && host.declaresTarget()
                && (((PermanentSubject.Target) equipment).group() == null
                || ((PermanentSubject.Target) host).group() == null)) {
            throw new IllegalArgumentException("A targeted Equipment and a targeted host need their own target groups");
        }
        if (targetPredicate != null && !equipment.declaresTarget() && !host.declaresTarget()) {
            throw new IllegalArgumentException("A target restriction needs a target subject");
        }
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                        boolean thenEffectOptionalTarget,
                                                        PermanentPredicate targetPredicate) {
        this(thenEffect, thenEffectOptionalTarget, targetPredicate, null, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect() {
        this(null, false, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect) {
        this(thenEffect, false, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                       boolean thenEffectOptionalTarget) {
        this(thenEffect, thenEffectOptionalTarget, null);
    }

    /** Attaches {@code equipment} to {@code host}. */
    public AttachSourceEquipmentToTargetCreatureEffect(PermanentSubject equipment, PermanentSubject host) {
        this(null, false, null, equipment, host);
    }

    /** Attaches {@code equipment} to {@code host}, the target restricted by {@code targetPredicate}. */
    public AttachSourceEquipmentToTargetCreatureEffect(PermanentSubject equipment, PermanentSubject host,
                                                       PermanentPredicate targetPredicate) {
        this(null, false, targetPredicate, equipment, host);
    }

    public static AttachSourceEquipmentToTargetCreatureEffect forCreatureYouControl() {
        return new AttachSourceEquipmentToTargetCreatureEffect(
                null, false, new PermanentControlledBySourceControllerPredicate());
    }

    /** Attaches to the triggering creature as a non-targeting reference. */
    public static AttachSourceEquipmentToTargetCreatureEffect forTriggeringCreature() {
        return new AttachSourceEquipmentToTargetCreatureEffect(Subjects.source(), Subjects.triggering());
    }

    /**
     * "Attach any number of target Equipment [you control] to it" after "whenever this or another creature enters /
     * attacks": every targeted Equipment to the creature the event was about, else to this creature.
     */
    public static AttachSourceEquipmentToTargetCreatureEffect targetEquipmentToTriggeringOrSource() {
        return new AttachSourceEquipmentToTargetCreatureEffect(Subjects.target(), Subjects.triggeringOrSource(),
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT));
    }

    private static boolean isNamedOne(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Source
                || subject instanceof PermanentSubject.Target
                || subject instanceof PermanentSubject.Triggering
                || subject instanceof PermanentSubject.TriggeringOrSource;
    }

    @Override
    public TargetSpec targetSpec() {
        if (host.declaresTarget() && equipment.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (host.declaresTarget()) {
            return targetPredicate == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
        }
        if (equipment.declaresTarget()) {
            return targetPredicate == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
        }
        return TargetSpec.NONE;
    }

    /** "Attach any number of target Equipment to it": nothing happens when no Equipment was targeted. */
    @Override
    public boolean actsOnlyOnItsTargets() {
        return equipment.declaresTarget() && !host.declaresTarget();
    }
}
