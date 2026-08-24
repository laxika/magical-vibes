package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** True when the targeted player, or the controller of the targeted planeswalker, controls a matching permanent. */
public record TargetPlayerControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "the targeted player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "the targeted player controls no matching permanent";
    }
}
