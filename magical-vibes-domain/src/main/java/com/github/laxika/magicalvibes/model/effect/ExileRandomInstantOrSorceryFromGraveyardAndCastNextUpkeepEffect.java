package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles an instant or sorcery card chosen at random from the controller's graveyard and offers it
 * for a free cast at the beginning of the controller's next upkeep. If cast, the spell is exiled
 * instead of being put into a graveyard.
 *
 * <p>The card is the trigger's target and is chosen at random as the ability is put onto the stack;
 * it is the stack entry's {@code targetId} (in the graveyard), and the ability does nothing if the card
 * has left the graveyard (CR 608.2b).</p>
 */
public record ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect() implements CardEffect {
}
