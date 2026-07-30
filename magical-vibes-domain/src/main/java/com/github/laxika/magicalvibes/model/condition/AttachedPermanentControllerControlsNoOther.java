package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller of the permanent the source Aura/Equipment is attached to controls no
 * permanent matching the predicate other than that attached permanent itself. Unlike
 * {@link NoOtherPermanent} — which is relative to the source's own controller and excludes
 * the source — this is relative to the attached permanent's controller, as required by
 * "as long as its controller controls no other creatures" (Predator's Gambit).
 */
public record AttachedPermanentControllerControlsNoOther(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "attached permanent's controller controls no other matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "the attached permanent's controller controls another matching permanent";
    }
}
