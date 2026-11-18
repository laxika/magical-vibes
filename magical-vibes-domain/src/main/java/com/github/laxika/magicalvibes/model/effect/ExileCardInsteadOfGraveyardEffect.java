package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "If that card [spell] would be put into a graveyard this turn, exile it instead": a replacement effect a resolution
 * makes (CR 611.2a, CR 614.1a) on one card it lets be cast, which follows the card onto the stack as the spell it
 * becomes. It is a floating effect on that card's caster, until end of turn, and is used up as the card is cast with it
 * (the spell then carries it) or is exiled by it.
 *
 * @param cardId the card it applies to
 */
public record ExileCardInsteadOfGraveyardEffect(UUID cardId) implements CardEffect {
}
