package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: permanents matching {@code scope} and {@code filter} become colorless.
 *
 * @param scope which permanents are affected (typically {@link GrantScope#ENCHANTED_PERMANENT});
 *              {@link GrantScope#ALL_PERMANENTS} affects every other permanent and uses the
 *              self-handler to include the source permanent
 * @param filter optional predicate restricting the affected permanents
 */
public record BecomeColorlessEffect(GrantScope scope, PermanentPredicate filter) implements CardEffect {

    public BecomeColorlessEffect(GrantScope scope) {
        this(scope, null);
    }
}
