package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: the source permanent becomes a copy of the creature card that was just put into an
 * opponent's graveyard, except its name stays the source's printed name, it is legendary in addition
 * to its other types, and it has hexproof and the triggered ability that granted this effect. Used by
 * Lazav, Dimir Mastermind.
 *
 * <p>The ability does not target and does not require the card to still be in the graveyard when it
 * resolves: the card is the stack entry's triggering card as it was put into the graveyard, copied as
 * last-known information.
 */
public record BecomeCopyOfCreatureCardInOpponentGraveyardEffect() implements CardEffect {
}
