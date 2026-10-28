package com.github.laxika.magicalvibes.model.condition;

/**
 * "If that spell is countered this way": the most recent counter step of the resolving entry actually
 * countered its target spell, as recorded in its resolution scratchpad
 * ({@code StackEntry.getResolutionResults()}). A counter that does nothing (uncounterable spell,
 * colour protection, spell already gone) fails the condition. Use it after the {@code CounterSpellEffect}
 * step, since the countered spell has left the stack by then.
 */
public record SpellCounteredThisWay() implements Condition {

    @Override
    public String conditionName() {
        return "that spell was countered this way";
    }

    @Override
    public String conditionNotMetReason() {
        return "the spell was not countered";
    }
}
