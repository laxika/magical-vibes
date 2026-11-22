package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: the permanents {@code subject} names that match {@code filter} have a supertype, as
 * {@link GrantCardTypeEffect} gives a card type ("all nonland permanents are legendary", Leyline of Singularity;
 * "permanents with ice counters on them are snow", Rimefeather Owl).
 */
public record GrantSupertypeEffect(CardSupertype supertype, PermanentSubject subject, PermanentPredicate filter)
        implements CardEffect {
}
