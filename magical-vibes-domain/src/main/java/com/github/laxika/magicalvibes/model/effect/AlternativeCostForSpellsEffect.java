package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect from a permanent on the battlefield that allows the controller
 * to pay an alternative mana cost for spells matching the given predicate.
 * This is an alternative cost per CR 118.9.
 * (e.g. Rooftop Storm: "You may pay {0} rather than pay the mana cost for Zombie creature spells you cast.")
 * (e.g. Jodah, Archmage Eternal: "You may pay {W}{U}{B}{R}{G} rather than pay the mana cost for spells you cast.")
 * A null filter matches all spells.
 *
 * <p>{@code manaValueCapCounter} (nullable) restricts the alternative cost to spells whose mana value
 * is at most the number of that counter on the source permanent — "a spell you cast with mana value X
 * or less, where X is the number of time counters on this enchantment" (As Foretold, {@code CounterType.TIME}).
 * {@code oncePerTurn} limits the source to a single use of the alternative cost each turn.
 *
 * <p>{@code fromHandOnly} restricts the alternative cost to spells cast from the controller's hand —
 * "you may cast spells from your hand without paying their mana costs" (Omniscience). Sources without
 * this restriction (Rooftop Storm, Jodah, As Foretold) apply regardless of the zone cast from.
 */
public record AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                             CounterType manaValueCapCounter, boolean oncePerTurn,
                                             boolean fromHandOnly) implements CardEffect {

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter) {
        this(manaCost, filter, null, false, false);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, false);
    }
}
