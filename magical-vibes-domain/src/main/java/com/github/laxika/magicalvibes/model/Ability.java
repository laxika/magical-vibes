package com.github.laxika.magicalvibes.model;

/**
 * An ability of a card, one of the four kinds of CR 113.3: a {@link SpellAbility} (113.3a), an
 * {@link ActivatedAbility} (113.3b), a {@link TriggeredAbility} (113.3c) or a {@link StaticAbility}
 * (113.3d). Abilities are data built in the card's constructor; the spell, triggered and static
 * kinds are immutable records, so a frozen card shares them with simulation copies as they are.
 * Code that uses abilities switches over this sealed hierarchy.
 */
public sealed interface Ability permits SpellAbility, ActivatedAbility, TriggeredAbility, StaticAbility {
}
