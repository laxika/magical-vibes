package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "As an additional cost to cast this spell, reveal a [subtype] card from your hand or pay {amount}."
 * The Lorwyn "reveal a creature-type card or pay" cycle (e.g. Goldmeadow Stalwart:
 * reveal a Kithkin card or pay {3}).
 *
 * <p>Modeled as a spell-self cost increase in the {@code STATIC} slot: the spell costs {@code amount}
 * more to cast unless the caster has a card of the given {@code subtype} in hand (other than the
 * spell being cast), in which case that card is revealed at no mana cost. Revealing never removes the
 * card, so auto-revealing whenever a matching card is available is game-state-equivalent to the
 * player choosing to reveal — the only option lost is paying {@code amount} despite holding a
 * matching card, which is never advantageous.
 */
public record IncreaseOwnCastCostUnlessRevealSubtypeEffect(int amount, CardSubtype subtype) implements CardEffect {
}
