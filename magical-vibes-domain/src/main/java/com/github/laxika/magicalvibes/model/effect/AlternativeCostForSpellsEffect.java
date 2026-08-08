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
 *
 * <p>{@code appliesToAllPlayers} offers the alternative cost to every player rather than only the
 * source's controller — "Any player may cast creature spells with mana value 3 or less without
 * paying their mana costs" (Aluren).
 *
 * <p>{@code genericEqualToManaValue} makes the alternative cost generic mana equal to the spell's own
 * mana value rather than the fixed {@code manaCost} string — "You may pay {X} rather than pay the mana
 * cost for Samurai spells you cast, where X is that spell's mana value" (Kentaro, the Smiling Cat).
 * Callers must resolve the cost through {@link #manaCostFor(int)} instead of reading {@code manaCost}.
 */
public record AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                             CounterType manaValueCapCounter, boolean oncePerTurn,
                                             boolean fromHandOnly, boolean appliesToAllPlayers,
                                             boolean genericEqualToManaValue) implements CardEffect {

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter) {
        this(manaCost, filter, null, false, false, false, false);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, false, false, false);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, false, false);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly, boolean appliesToAllPlayers) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, appliesToAllPlayers, false);
    }

    /**
     * The alternative mana cost string actually payable for a spell with the given mana value:
     * generic mana equal to that mana value when {@code genericEqualToManaValue} is set, otherwise
     * the fixed printed cost.
     */
    public String manaCostFor(int spellManaValue) {
        return genericEqualToManaValue ? "{" + spellManaValue + "}" : manaCost;
    }

    /**
     * An alternative cost of generic mana equal to the spell's own mana value, for spells matching
     * the filter (Kentaro, the Smiling Cat).
     */
    public static AlternativeCostForSpellsEffect genericEqualToManaValue(CardPredicate filter) {
        return new AlternativeCostForSpellsEffect("{0}", filter, null, false, false, false, true);
    }
}
