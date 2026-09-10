package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Advances a Class permanent to the specified level and queues the abilities it gains at that
 * level. The level is stored using the engine's existing level state; it is not a real counter in
 * the card's rules text.
 */
public record ClassLevelUpEffect(int level, List<CardEffect> gainedEffects) implements CardEffect {

    public ClassLevelUpEffect(int level) {
        this(level, List.of());
    }
}
