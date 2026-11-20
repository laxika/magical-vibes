package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: permanents matching {@code subject} and {@code filter} become colorless.
 *
 * @param subject which permanents are affected (typically {@link Subjects#attached()});
 *              {@link Subjects#otherPermanents()} affects every other permanent and uses the
 *              self-handler to include the source permanent
 * @param filter optional predicate restricting the affected permanents
 */
public record BecomeColorlessEffect(PermanentSubject subject, PermanentPredicate filter) implements CardEffect {

    public BecomeColorlessEffect(PermanentSubject subject) {
        this(subject, null);
    }
}
