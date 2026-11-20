package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

/**
 * Death trigger for transferring every counter of a dying creature to a creature: target creature (the default;
 * e.g. Scolding Administrator, Host of the Hereafter, and Parish-Blade Trainee) or, for {@link #toSource()}, this
 * permanent ("put its counters on this creature", Buzzard-Wasp Colony).
 * <p>
 * The counters are the stack entry's triggering permanent's, as it last existed on the battlefield
 * (CR 603.10a). "If it had counters on it" is the trigger's filter
 * ({@code Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)}); without one the
 * ability triggers and places no counters. Resolution places each counter on the named creature.
 *
 * @param controllerOnly whether the target must be a creature controlled by the trigger controller
 * @param subject        {@link Subjects#target()} or {@link Subjects#source()}
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly, PermanentSubject subject)
        implements CardEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (!(subject instanceof PermanentSubject.Target) && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Moves the counters to the target or the source: " + subject);
        }
        if (controllerOnly && !subject.declaresTarget()) {
            throw new IllegalArgumentException("Only a target is restricted to creatures you control");
        }
    }

    public MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly) {
        this(controllerOnly, Subjects.target());
    }

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(false);
    }

    /** "Put its counters on this creature." */
    public static MoveDyingSourceCountersToTargetCreatureEffect toSource() {
        return new MoveDyingSourceCountersToTargetCreatureEffect(false, Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        if (!subject.declaresTarget()) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.benign(TargetPredicates.permanent(), controllerOnly
                ? new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()))
                : new PermanentIsCreaturePredicate());
    }
}
