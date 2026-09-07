package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

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
 * {@code manaValueCapAmount} provides the cap from a dynamic game-state amount, such as the
 * number of lands controlled by the caster (Fires of Invention). Only one cap form is normally set.
 * {@code oncePerTurn} limits the source to a single use of the alternative cost each turn.
 * {@code controllerTurnOnly} restricts the alternative cost to the source controller's turn.
 * {@code allowedZones} optionally restricts the alternative cost to casts from specific zones.
 *
 * <p>{@code fromHandOnly} restricts the alternative cost to spells cast from the controller's hand —
 * "you may cast spells from your hand without paying their mana costs" (Omniscience). Sources without
 * this restriction (Rooftop Storm, Jodah, As Foretold) apply regardless of the zone cast from unless
 * {@code allowedZones} restricts them.
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
                                             boolean genericEqualToManaValue, boolean controllerTurnOnly,
                                             Set<Zone> allowedZones, CostEffect nonManaCost,
                                             DynamicAmount manaValueCapAmount, boolean castsWithWarp)
        implements CardEffect {

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly, boolean appliesToAllPlayers,
                                          boolean genericEqualToManaValue, boolean controllerTurnOnly,
                                          Set<Zone> allowedZones, CostEffect nonManaCost,
                                          DynamicAmount manaValueCapAmount) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, appliesToAllPlayers,
                genericEqualToManaValue, controllerTurnOnly, allowedZones, nonManaCost,
                manaValueCapAmount, false);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter) {
        this(manaCost, filter, null, false, false, false, false, false, null, null, null);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, false, false, false, false, null, null, null);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, false, false, false, null, null, null);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly, boolean appliesToAllPlayers) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, appliesToAllPlayers,
                false, false, null, null, null);
    }

    public AlternativeCostForSpellsEffect(String manaCost, CardPredicate filter,
                                          CounterType manaValueCapCounter, boolean oncePerTurn,
                                          boolean fromHandOnly, boolean appliesToAllPlayers,
                                          boolean genericEqualToManaValue, boolean controllerTurnOnly,
                                          Set<Zone> allowedZones) {
        this(manaCost, filter, manaValueCapCounter, oncePerTurn, fromHandOnly, appliesToAllPlayers,
                genericEqualToManaValue, controllerTurnOnly, allowedZones, null, null);
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
        return new AlternativeCostForSpellsEffect("{0}", filter, null, false, false, false,
                true, false, null, null, null);
    }

    /** A zero alternative cost usable once during each turn of the source controller. */
    public static AlternativeCostForSpellsEffect onceDuringControllerTurn(CardPredicate filter) {
        return new AlternativeCostForSpellsEffect("{0}", filter, null, true, false, false, false, true,
                Set.of(Zone.HAND, Zone.LIBRARY), null, null);
    }

    /** An alternative cost that is paid by collecting evidence rather than paying mana. */
    public static AlternativeCostForSpellsEffect collectEvidence(int minimumManaValue,
                                                                  CardPredicate filter) {
        return new AlternativeCostForSpellsEffect(null, filter, null, false, false, false,
                false, false, null, new CollectEvidenceCost(minimumManaValue), null);
    }

    /** A zero alternative cost for spells whose mana value is at most a dynamic amount. */
    public static AlternativeCostForSpellsEffect zeroManaValueAtMost(CardPredicate filter,
                                                                     DynamicAmount cap) {
        return new AlternativeCostForSpellsEffect("{0}", filter, null, false, false, false,
                false, true, null, null, cap);
    }

    /** An alternative cost from hand that casts the resulting permanent with Warp. */
    public static AlternativeCostForSpellsEffect warp(String manaCost, CardPredicate filter) {
        return new AlternativeCostForSpellsEffect(manaCost, filter, null, false, true, false,
                false, false, Set.of(Zone.HAND), null, null, true);
    }
}
