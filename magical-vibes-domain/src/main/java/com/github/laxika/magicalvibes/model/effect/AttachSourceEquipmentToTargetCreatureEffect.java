package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The attach verb (CR 701.3a): attaches each Aura, Equipment or Fortification {@link #equipment()} names to one
 * permanent ({@link #host()}) or one player ({@link #playerHost()}). The default is "attach this Equipment to target
 * creature [you control]": {@code (source(), target())}. The other shapes:
 * <ul>
 *   <li>{@code (source(), triggering())}: "attach it to that creature" ({@link #forTriggeringCreature()}), "attach this
 *       Aura to that creature";</li>
 *   <li>{@code (target(g), target(g'))}: "attach target Equipment / Aura to target creature" (the two target groups);</li>
 *   <li>{@code (target(), source())}: "attach target Equipment you control to this creature";</li>
 *   <li>{@code (target(), triggeringOrSource())}: "attach any number of target Equipment to it" after "whenever this
 *       or another creature enters / attacks" (every targeted Equipment);</li>
 *   <li>{@code (triggering(), target())}: "attach that Equipment to target creature" after "whenever an Equipment
 *       enters";</li>
 *   <li>a set of attachments ({@code controlledBy(you(), Equipment)}: "attach all Equipment you control to this
 *       creature"; {@code thisWay(CHOOSE)}: the Auras or Equipment a {@code ChoosePermanentsEffect} chose) and a host an
 *       earlier step named ({@code thisWay(CHOOSE)}, {@code thisWay(MANIFEST)}, {@code returnedThisResolution()});</li>
 *   <li>a player host ({@link #toPlayer}): "attach this Aura to that player", "attach a Curse to one of your
 *       opponents".</li>
 * </ul>
 * An Aura is attached only to an object or player it could enchant (its enchant ability, protection, "can't be
 * enchanted"; never itself, never while it is a creature, CR 303.4d), an Equipment only to a creature it could equip
 * (CR 301.5c: not itself, not while it is a creature without reconfigure; protection, CR 702.16d), a Fortification only
 * to a land (CR 301.6). One that can't be attached to the host doesn't move, one already attached to it does nothing,
 * and an object that is none of the three doesn't move (CR 701.3b). One that moves gets a new timestamp (CR 613.7e) and
 * control is recomputed when it carries a control effect (an Aura's "you control enchanted creature"). The verb records
 * {@link com.github.laxika.magicalvibes.model.StepKind#ATTACH}: the attachments that moved. An optional continuation is
 * queued as a reflexive ability once an attachment is attached to the host, one already attached included.
 *
 * @param targetPredicate the target restriction: of the host target ("target creature you control"), or, when only
 *                        the attachment is targeted, of the attachment target
 * @param equipment       the Auras, Equipment and Fortifications to attach
 * @param host            the permanent they are attached to; {@code null} with a player host
 * @param playerHost      the player they are attached to; {@code null} with a permanent host
 */
public record AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                          boolean thenEffectOptionalTarget,
                                                          PermanentPredicate targetPredicate,
                                                          PermanentSubject equipment,
                                                          PermanentSubject host,
                                                          PlayerSubject playerHost)
        implements CardEffect {

    public AttachSourceEquipmentToTargetCreatureEffect {
        equipment = equipment == null ? Subjects.source() : equipment;
        host = host == null && playerHost == null ? Subjects.target() : host;
        if (host != null && playerHost != null) {
            throw new IllegalArgumentException("An attach effect names one host: " + host + ", " + playerHost);
        }
        if (host != null && !isOneHost(host)) {
            throw new IllegalArgumentException("An attach effect names one host permanent: " + host);
        }
        if (playerHost != null && playerHost.declaresTarget()) {
            throw new IllegalArgumentException("A player host is named, not targeted: " + playerHost);
        }
        if (equipment.declaresTarget() && host != null && host.declaresTarget()
                && (!(equipment instanceof PermanentSubject.Target equipmentTarget)
                || !(host instanceof PermanentSubject.Target hostTarget)
                || equipmentTarget.group() == null || hostTarget.group() == null)) {
            throw new IllegalArgumentException("A targeted attachment and a targeted host need their own target groups");
        }
        if (targetPredicate != null && !equipment.declaresTarget() && (host == null || !host.declaresTarget())) {
            throw new IllegalArgumentException("A target restriction needs a target subject");
        }
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                       boolean thenEffectOptionalTarget,
                                                       PermanentPredicate targetPredicate,
                                                       PermanentSubject equipment,
                                                       PermanentSubject host) {
        this(thenEffect, thenEffectOptionalTarget, targetPredicate, equipment, host, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                        boolean thenEffectOptionalTarget,
                                                        PermanentPredicate targetPredicate) {
        this(thenEffect, thenEffectOptionalTarget, targetPredicate, null, null, null);
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
        this(null, false, null, equipment, host, null);
    }

    /** Attaches {@code equipment} to {@code host}, the target restricted by {@code targetPredicate}. */
    public AttachSourceEquipmentToTargetCreatureEffect(PermanentSubject equipment, PermanentSubject host,
                                                       PermanentPredicate targetPredicate) {
        this(null, false, targetPredicate, equipment, host, null);
    }

    /** "Attach this Aura to that player": attaches {@code attachments} to the player {@code player} names. */
    public static AttachSourceEquipmentToTargetCreatureEffect toPlayer(PermanentSubject attachments,
                                                                       PlayerSubject player) {
        return new AttachSourceEquipmentToTargetCreatureEffect(null, false, null, attachments, null,
                java.util.Objects.requireNonNull(player, "player"));
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

    /**
     * "If an Equipment is put onto the battlefield this way, you may attach it to a creature you control" (Armored
     * Skyhunter, Vault 101: Birthday Party; the "you may" is the caller's {@code MayEffect}): a creature you control that
     * the picked Equipment ({@link Subjects#picked(PermanentPredicate)}) could equip is chosen, unasked when only one
     * could (CR 608.2d), and the Equipment is attached to it.
     */
    public static SequenceEffect pickedEquipmentToCreatureYouControl() {
        PermanentSubject equipment = Subjects.picked(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT));
        return SequenceEffect.of(
                ChoosePermanentsEffect.exactly(1, Subjects.creaturesYouControl(), "a creature you control")
                        .thatCanHost(equipment),
                new AttachSourceEquipmentToTargetCreatureEffect(equipment, Subjects.thisWay(StepKind.CHOOSE)));
    }

    /** A host is one permanent: named by the entry, or the one an earlier step of the resolution named. */
    private static boolean isOneHost(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Source
                || subject instanceof PermanentSubject.Target
                || subject instanceof PermanentSubject.Triggering
                || subject instanceof PermanentSubject.TriggeringOrSource
                || subject instanceof PermanentSubject.ThisWay
                || subject instanceof PermanentSubject.ReturnedThisResolution;
    }

    @Override
    public TargetSpec targetSpec() {
        boolean hostTargeted = host != null && host.declaresTarget();
        if (hostTargeted && equipment.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (hostTargeted) {
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
        return equipment.declaresTarget() && (host == null || !host.declaresTarget());
    }
}
