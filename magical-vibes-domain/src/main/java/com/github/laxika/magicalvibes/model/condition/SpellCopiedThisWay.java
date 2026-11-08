package com.github.laxika.magicalvibes.model.condition;

/**
 * "If you don't copy a spell this way" (negated with {@link NotCondition}): the most recent copy step of the resolving
 * entry ({@code CopyTriggeringSpellEffect}) copied its spell, as recorded in its resolution scratchpad
 * ({@code StackEntry.getResolutionResults()}). A copy step that copies nothing (no spell, a spell that can't be
 * copied) fails the condition, and so does an entry with no copy step. Use it after the copy step.
 */
public record SpellCopiedThisWay() implements Condition {

    @Override
    public String conditionName() {
        return "a spell was copied this way";
    }

    @Override
    public String conditionNotMetReason() {
        return "no spell was copied";
    }
}
