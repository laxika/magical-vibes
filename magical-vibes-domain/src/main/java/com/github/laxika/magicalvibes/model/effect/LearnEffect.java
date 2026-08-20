package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves Learn: offer a Lesson from outside the game, or discard a card to draw a card.
 *
 * <p>The excluded card ids are used when a player declines one of several simultaneous Learn
 * replacement choices in graveyards.</p>
 */
public record LearnEffect(boolean checkReplacement, Set<UUID> excludedReplacementCardIds) implements CardEffect {

    public LearnEffect() {
        this(true, Set.of());
    }

    public LearnEffect(boolean checkReplacement) {
        this(checkReplacement, Set.of());
    }
}
