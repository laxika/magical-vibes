package com.github.laxika.magicalvibes.model.condition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Logical conjunction of several conditions — met exactly when every inner condition is met.
 * Lets a card compose a compound intervening-"if" from existing leaf conditions rather than
 * introducing a bespoke condition (e.g. Erg Raiders' "didn't attack this turn" and
 * "not came under your control this turn"). Evaluation recurses into each inner condition
 * with the same {@code ConditionContext}.
 */
public record AllOf(List<Condition> conditions) implements Condition {

    @Override
    public String conditionName() {
        return conditions.stream().map(Condition::conditionName).collect(Collectors.joining(" and "));
    }

    @Override
    public String conditionNotMetReason() {
        return conditions.stream().map(Condition::conditionNotMetReason).collect(Collectors.joining(" and "));
    }
}
