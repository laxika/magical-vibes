package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Sacrifices one named permanent: by default the stack entry's source permanent ("sacrifice this creature"). The
 * {@code subject} names the permanent: {@link Subjects#source()}, {@link Subjects#triggering()} ("sacrifice that
 * permanent", read from the stack entry's triggering permanent; Grafted Wargear, Stitcher's Graft, the delayed
 * "sacrifice it at the beginning of the next end step" triggers), {@link Subjects#attached()} ("sacrifice it" after
 * "if equipped creature didn't deal combat damage", Thirsting Axe), {@link Subjects#attachedAtTrigger()} ("that player
 * sacrifices that creature" after "at the beginning of the upkeep of enchanted creature's controller": the creature
 * the Aura enchanted as the ability triggered) or {@link Subjects#target()} / {@code target(group)}
 * ("target creature you control deals damage ... Then sacrifice it"; the only subject that declares a target),
 * {@link Subjects#triggeringOrSource()} ("sacrifice it" after "whenever this creature or equipped creature deals
 * combat damage", Ogre-Head Helm), {@link Subjects#granter()} ("sacrifice Spare Dagger" in an ability the Equipment
 * grants) or {@code thisWay(StepKind.CHOOSE)} ("that player sacrifices one of them of their choice" after a
 * {@link ChoosePermanentsEffect}, sacrificed by {@code controllerOf(thisWay(CHOOSE))}: Incriminate, Barrin's Spite).
 *
 * <p>"Sacrifice it. If you do, ..." is this effect followed by a {@code ConditionalEffect} of
 * {@code HappenedThisWay.sacrificed()} (in a {@link SequenceEffect} where the steps must stay one effect); "when you
 * do" wraps the payload in a {@link QueueReflexiveAbilityEffect}.
 *
 * <p>{@code sacrificer} is the player told to sacrifice it: {@link Subjects#you()} by default,
 * {@code controllerOf(subject)} for "that creature's controller sacrifices it" (Basalt Golem), or the player a step
 * trigger was about ("that player sacrifices it": {@link Subjects#activePlayer()} on the upkeep or end step of the
 * enchanted permanent's controller, {@link Subjects#triggeringPlayer()} for a damaged player). A player can sacrifice
 * only a permanent they control, so nothing happens unless the sacrificer controls it as the effect resolves. An
 * optional token rider is created for that player only when the sacrifice succeeds ("if the player does, they create
 * ..."). If the permanent has left the battlefield, nothing happens.
 *
 * @param subject                   which permanent is sacrificed
 * @param sacrificer                who sacrifices it
 * @param tokenForSacrificingPlayer created for the sacrificing player if they do, or {@code null}
 * @param targetFilter              for a {@code target()} subject, what the target must be (it is still checked as
 *                                  the effect resolves); {@code null} means a creature
 */
public record SacrificeSelfEffect(PermanentSubject subject,
                                  PlayerSubject sacrificer,
                                  CreateTokenEffect tokenForSacrificingPlayer,
                                  PermanentPredicate targetFilter)
        implements CardEffect {

    public SacrificeSelfEffect {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(sacrificer, "sacrificer");
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering
                || subject instanceof PermanentSubject.Attached || subject instanceof PermanentSubject.AttachedAtTrigger
                || subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.TriggeringOrSource
                || subject instanceof PermanentSubject.Granter
                || subject instanceof PermanentSubject.ThisWay thisWay && thisWay.kind() == StepKind.CHOOSE)) {
            throw new IllegalArgumentException("Sacrifices the source, the triggering (or else the source), the "
                    + "attached (now or at trigger), the granting, the chosen or the target permanent: " + subject);
        }
        if (targetFilter != null && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("A target filter needs a target subject: " + subject);
        }
    }

    public SacrificeSelfEffect() {
        this(false, null);
    }

    public SacrificeSelfEffect(boolean currentControllerSacrifices) {
        this(currentControllerSacrifices, null);
    }

    public SacrificeSelfEffect(boolean currentControllerSacrifices, CreateTokenEffect tokenForSacrificingPlayer) {
        this(Subjects.source(), sacrificerOf(Subjects.source(), currentControllerSacrifices), tokenForSacrificingPlayer,
                null);
    }

    /** {@code subject}, sacrificed by the ability's controller. */
    public SacrificeSelfEffect(PermanentSubject subject) {
        this(subject, Subjects.you());
    }

    /** {@code subject}, sacrificed by {@code sacrificer}. */
    public SacrificeSelfEffect(PermanentSubject subject, PlayerSubject sacrificer) {
        this(subject, sacrificer, null, null);
    }

    /** A target permanent matching {@code targetFilter}, sacrificed by {@code sacrificer}. */
    public SacrificeSelfEffect(PermanentSubject subject, PlayerSubject sacrificer, PermanentPredicate targetFilter) {
        this(subject, sacrificer, null, targetFilter);
    }

    /** "Sacrifice that permanent": the stack entry's triggering permanent, by the ability's controller. */
    public static SacrificeSelfEffect ofTriggeringPermanent() {
        return ofTriggeringPermanent(false, null);
    }

    /**
     * The triggering permanent, sacrificed by its current controller when {@code currentControllerSacrifices},
     * with {@code tokenForSacrificingPlayer} (or {@code null}) created for that player if they do.
     */
    public static SacrificeSelfEffect ofTriggeringPermanent(boolean currentControllerSacrifices,
                                                            CreateTokenEffect tokenForSacrificingPlayer) {
        return new SacrificeSelfEffect(Subjects.triggering(),
                sacrificerOf(Subjects.triggering(), currentControllerSacrifices), tokenForSacrificingPlayer, null);
    }

    private static PlayerSubject sacrificerOf(PermanentSubject subject, boolean currentControllerSacrifices) {
        return currentControllerSacrifices ? Subjects.controllerOf(subject) : Subjects.you();
    }

    /** Whether this sacrifices the ability's own source permanent. */
    public boolean sacrificesSource() {
        return subject instanceof PermanentSubject.Source;
    }

    @Override
    public int targetGroup() {
        return subject instanceof PermanentSubject.Target target && target.group() != null ? target.group() : -1;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.harmful(targetFilter == null ? TargetPredicates.creature()
                    : TargetPredicates.permanents(targetFilter));
        }
        // The source form implicitly acts on its own source permanent — so trigger collectors that key off
        // selfTargeting() (e.g. the spell-cast collector) carry the source id onto the stack entry.
        return sacrificesSource() || subject instanceof PermanentSubject.TriggeringOrSource
                ? new TargetSpec(null, false, null, true, 1) : TargetSpec.NONE;
    }
}
