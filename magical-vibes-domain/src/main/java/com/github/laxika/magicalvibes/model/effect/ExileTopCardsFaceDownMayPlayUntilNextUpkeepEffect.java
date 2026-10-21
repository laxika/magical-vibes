package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top {@code count} cards of the controller's library face down. The controller may look
 * at those cards for as long as they remain exiled, and may play them until their next turn
 * begins. At the beginning of their next upkeep, any still-exiled cards are put into their owner's graveyard (via
 * {@code ExileToOwnerGraveyardAtNextUpkeep}, resolved as one trigger).
 * <p>
 * Used by Three Wishes. Cleanup matches Grinning Totem
 * (unplayed → owner's graveyard) rather than Elkin Bottle (stays in exile).
 */
public record ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect(int count) implements CardEffect {
}
