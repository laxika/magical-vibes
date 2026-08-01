package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top {@code count} cards of the controller's library face down. The controller may look
 * at those cards for as long as they remain exiled, and may play them until the beginning of their
 * next upkeep. At that upkeep, any still-exiled cards are put into their owner's graveyard (via
 * {@code ExileToOwnerGraveyardAtNextUpkeep}).
 * <p>
 * Used by Three Wishes. Play-window timing matches {@link ExileTopCardMayPlayUntilNextUpkeepEffect}
 * (Elkin Bottle) and Grinning Totem's {@code expiresAtNextUpkeep}; cleanup matches Grinning Totem
 * (unplayed → owner's graveyard) rather than Elkin Bottle (stays in exile).
 */
public record ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect(int count) implements CardEffect {
}
