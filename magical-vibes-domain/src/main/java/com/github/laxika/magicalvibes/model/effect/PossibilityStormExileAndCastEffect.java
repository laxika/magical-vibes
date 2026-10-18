package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Resolution effect for Possibility Storm's triggered ability: the casting player exiles the
 * triggering spell, then exiles cards from the top of their library until one shares a card type
 * with it, may cast that card without paying its mana cost, and finally puts every card exiled with
 * the enchantment on the bottom of their library in a random order.
 *
 * <p>The trigger is controlled by Possibility Storm's controller, but every choice belongs to the
 * player who cast the spell, so {@code castingPlayerId} tracks them separately. The ability still
 * works if the enchantment has left the battlefield by the time it resolves — the exile tracking is
 * keyed by {@code sourcePermanentId} alone.</p>
 *
 * <p>{@code originalSpellCard} is the spell as it was cast. If the spell has already left the stack when
 * the ability resolves (another Possibility Storm exiled it first), its card types are still used for
 * the library search.</p>
 */
public record PossibilityStormExileAndCastEffect(
        UUID originalSpellCardId,
        Card originalSpellCard,
        UUID sourcePermanentId,
        UUID castingPlayerId
) implements CardEffect {
}
