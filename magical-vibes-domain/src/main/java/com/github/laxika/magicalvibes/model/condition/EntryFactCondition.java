package com.github.laxika.magicalvibes.model.condition;

/**
 * A condition that is a fact fixed as the permanent entered the battlefield: how it was cast (kicked, the zone it was
 * cast from, an alternative or additional cost paid, the mana spent to cast it, a gift promised), where it came from,
 * whether it is a token, and for an own enters ability the controller's main phase it entered during (Addendum). As the intervening "if" (CR 603.4) of a "when this enters" ability it is judged as the ability
 * triggers, from the entering permanent; as the ability resolves the answer is the same, and the stack entry can't
 * read how the permanent was cast, so the wrapper comes off once judged. As an effect's "if" it is judged then too, and
 * a false one leaves an effect that does nothing in its place, so the ability still goes on the stack.
 */
public interface EntryFactCondition {

    /**
     * Whether {@code condition} is a fact fixed as the permanent entered: an {@link EntryFactCondition}, its
     * {@link NotCondition}, or an {@link AllOf} / {@link AllConditions} / {@link AnyOf} of nothing else.
     */
    static boolean isEntryFact(Condition condition) {
        return switch (condition) {
            case EntryFactCondition ignored -> true;
            case NotCondition not -> isEntryFact(not.inner());
            case AllOf all -> !all.conditions().isEmpty()
                    && all.conditions().stream().allMatch(EntryFactCondition::isEntryFact);
            case AllConditions all -> !all.conditions().isEmpty()
                    && all.conditions().stream().allMatch(EntryFactCondition::isEntryFact);
            case AnyOf any -> !any.conditions().isEmpty()
                    && any.conditions().stream().allMatch(EntryFactCondition::isEntryFact);
            case null, default -> false;
        };
    }
}
