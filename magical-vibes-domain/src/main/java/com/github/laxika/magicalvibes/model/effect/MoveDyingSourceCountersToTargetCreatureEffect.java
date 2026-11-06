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
 * (CR 603.10a). When {@code requiresCounters} is true, "when this dies" doesn't trigger if it had no
 * counters; otherwise the trigger still requires a target and simply places no counters. Resolution
 * places each counter on the chosen creature.
 *
 * @param controllerOnly whether the target must be a creature controlled by the trigger controller
 * @param requiresCounters whether "when this dies" triggers only if the creature had counters on it
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly, boolean requiresCounters)
        implements CardEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(false, true);
    }

    public MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly) {
        this(controllerOnly, true);
    }

    /** Creates the mandatory-target form that also triggers when the source had no counters. */
    public static MoveDyingSourceCountersToTargetCreatureEffect alwaysTriggers(boolean controllerOnly) {
        return new MoveDyingSourceCountersToTargetCreatureEffect(controllerOnly, false);
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
