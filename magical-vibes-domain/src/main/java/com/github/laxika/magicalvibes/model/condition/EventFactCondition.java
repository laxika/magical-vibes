package com.github.laxika.magicalvibes.model.condition;

/**
 * A condition that is a fact about a trigger event as it happened: the attack as declared ({@link
 * DeclaredAttackCondition}), the mana spent to activate the ability whose activation triggered (Vazi, Keen
 * Negotiator's "if mana from a Treasure was spent to activate it"). As an intervening "if" (CR 603.4) it is judged as
 * the ability triggers; as the ability resolves the answer is the same, as the event already happened, so the stack door
 * doesn't wrap the ability's effects in it.
 */
public interface EventFactCondition {

    /**
     * Whether {@code condition} is a fact about the trigger event: an {@link EventFactCondition}, or an {@link AllOf} /
     * {@link AllConditions} of nothing else.
     */
    static boolean isEventFact(Condition condition) {
        return switch (condition) {
            case EventFactCondition ignored -> true;
            case AllOf all -> !all.conditions().isEmpty()
                    && all.conditions().stream().allMatch(EventFactCondition::isEventFact);
            case AllConditions all -> !all.conditions().isEmpty()
                    && all.conditions().stream().allMatch(EventFactCondition::isEventFact);
            case null, default -> false;
        };
    }
}
