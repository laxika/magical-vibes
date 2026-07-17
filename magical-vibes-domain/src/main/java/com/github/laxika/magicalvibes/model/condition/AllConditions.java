package com.github.laxika.magicalvibes.model.condition;

import java.util.List;

/**
 * Logical conjunction of several conditions — met exactly when every {@code condition} is met.
 * Evaluation recurses into each inner condition with the same {@code ConditionContext}
 * (e.g. Qasali Ambusher's "a creature is attacking you and you control a Forest and a Plains").
 */
public record AllConditions(List<Condition> conditions) implements Condition {

    @Override
    public String conditionName() {
        return "all of: " + conditions.stream().map(Condition::conditionName).toList();
    }

    @Override
    public String conditionNotMetReason() {
        return "not all required conditions are met";
    }
}
