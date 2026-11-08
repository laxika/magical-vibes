package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * The spell a cast trigger is about matches {@code filter} as the effect resolves: "copy that spell if it targets a
 * permanent or player". It reads the stack entry's triggering spell, live while it is on the stack and as it last
 * existed there once it has left (CR 608.2h), judged from the ability's controller and source. A condition of the
 * cast itself is the trigger's own ({@code Triggers.youCastSpell().withStackEntryFilter(...)}), not this.
 */
public record TriggeringSpellMatches(StackEntryPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "triggering spell matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering spell does not match " + filter;
    }
}
