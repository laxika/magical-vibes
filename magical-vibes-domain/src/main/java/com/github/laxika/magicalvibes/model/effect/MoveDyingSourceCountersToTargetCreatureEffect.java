package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

/**
 * Death trigger for transferring every counter of a dying creature to a target creature
 * (e.g. Scolding Administrator, Host of the Hereafter, and Parish-Blade Trainee).
 * <p>
 * The counters are the stack entry's triggering permanent's, as it last existed on the battlefield
 * (CR 603.10a). "If it had counters on it" is the trigger's filter
 * ({@code Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)}); without one the
 * ability triggers and places no counters. Resolution places each counter on the chosen creature.
 *
 * @param controllerOnly whether the target must be a creature controlled by the trigger controller
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly) implements CardEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), controllerOnly
                ? new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()))
                : new PermanentIsCreaturePredicate());
    }
}
