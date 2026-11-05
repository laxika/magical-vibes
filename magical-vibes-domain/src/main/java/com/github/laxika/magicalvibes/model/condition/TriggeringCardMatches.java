package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The card that triggered the ability matches the predicate as the effect resolves: "if that creature was a Villain",
 * "if that spell's power is 4 or greater", "if it's a Samurai card". It reads the stack entry's triggering card, the
 * card in the zone it moved to for a zone change, or the spell for a cast trigger. A filter on what triggers the
 * ability is the trigger's own ({@code Triggers.creatureDies(...).withFilter(...)}), not this.
 */
public record TriggeringCardMatches(CardPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "triggering card matches " + predicate;
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering card does not match " + predicate;
    }
}
