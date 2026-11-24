package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.Set;
import java.util.UUID;

/**
 * A redirection of one source's damage (CR 614.9), as the floating effect that carries it holds it until end of turn:
 * "the next time [source] would deal [combat] damage [to you / to an opponent] this turn, it deals that damage to
 * [destination] instead", or "all [combat] damage [source] would deal [to you] this turn". The source is the object
 * chosen or named as the effect was created (CR 609.7a): a permanent, or a spell by its card's id, which also covers
 * the permanent that spell becomes. Read by {@code DamagePreventionService.applicableSourceKeyedRedirects}.
 *
 * @param sourceId           the id the source goes by: a permanent's id, or a spell's card id
 * @param sourceStay         for a spell source, its card's graveyard-entry version as the shield was made, so a later
 *                           cast of that card is a new object the shield doesn't cover (CR 400.7); {@code -1} otherwise
 * @param protectedPlayerIds the only recipients whose damage is redirected, all of them players; {@code null} for
 *                           damage to any player or permanent
 * @param destinationId      the player or permanent the damage is dealt to instead; {@code null} for the source's
 *                           controller as it deals the damage
 * @param allThisTurn        whether it redirects all such damage this turn rather than the next time only
 * @param combatOnly         whether it redirects combat damage only
 */
public record SourceKeyedRedirectShield(
        UUID sourceId,
        long sourceStay,
        Set<UUID> protectedPlayerIds,
        UUID destinationId,
        boolean allThisTurn,
        boolean combatOnly) implements CardEffect {
}
