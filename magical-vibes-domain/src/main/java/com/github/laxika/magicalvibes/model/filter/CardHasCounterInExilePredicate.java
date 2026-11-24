package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/**
 * "With a [kind] counter on it" of a card in exile ("a card you own in exile with a silver counter on it", "cards in
 * exile your opponents own with ice counters on them"): the card has at least one {@code kind} counter in the one
 * store for counters on exiled cards ({@code GameData.exiledCardCounters}); needs the game state. A card exiled face
 * down is judged by a subject without its identity, so it never matches.
 *
 * @param kind the kind of counter
 */
public record CardHasCounterInExilePredicate(CounterType kind) implements CardPredicate {

    public CardHasCounterInExilePredicate {
        Objects.requireNonNull(kind, "kind");
    }
}
