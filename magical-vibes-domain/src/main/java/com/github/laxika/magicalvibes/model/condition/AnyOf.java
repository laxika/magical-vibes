package com.github.laxika.magicalvibes.model.condition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Logical disjunction of several conditions — met exactly when at least one inner condition is met.
 * The OR analogue of {@link AllOf}; lets a card compose a compound intervening-"if" from existing
 * leaf conditions rather than introducing a bespoke condition (e.g. Desert's Hold's "if you control
 * a Desert or there is a Desert card in your graveyard"). Evaluation recurses into each inner
 * condition with the same {@code ConditionContext}.
 */
public record AnyOf(List<Condition> conditions) implements Condition {

    @Override
    public String conditionName() {
        return conditions.stream().map(Condition::conditionName).collect(Collectors.joining(" or "));
    }

    @Override
    public String conditionNotMetReason() {
        return conditions.stream().map(Condition::conditionNotMetReason).collect(Collectors.joining(" and "));
    }

    /**
     * Acts as an ETB intervening-"if" gate (CR 603.4) only when every inner condition is itself a
     * game-state gate — otherwise a cast-time-choice condition (e.g. Kicked) buried inside an OR
     * would be silently treated as a gate.
     */
    @Override
    public boolean isEtbTriggerGate() {
        return !conditions.isEmpty() && conditions.stream().allMatch(Condition::isEtbTriggerGate);
    }
}
