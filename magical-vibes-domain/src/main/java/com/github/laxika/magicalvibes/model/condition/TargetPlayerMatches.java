package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;

/**
 * The player the condition is judged for (the context's target player, or the controller of a targeted permanent)
 * matches the predicate: "defending player controls the most creatures or is tied for the most" (Hooded Horror),
 * "unless defending player controls three or more creatures that share a creature type" (Graxiplon, negated).
 */
public record TargetPlayerMatches(PlayerPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "that player matches " + predicate;
    }

    @Override
    public String conditionNotMetReason() {
        return "that player does not match " + predicate;
    }
}
