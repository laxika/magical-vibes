package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles an instant or sorcery card chosen at random from the controller's graveyard and offers it
 * for a free cast at the beginning of the controller's next upkeep. If cast, the spell is exiled
 * instead of being put into a graveyard.
 *
 * <p>The card is the trigger's target and is chosen at random as the ability is put onto the stack;
 * {@code chosenCardId} carries that choice to resolution, where the ability does nothing if the card
 * has left the graveyard (CR 608.2b). It is {@code null} on the card definition.</p>
 *
 * @param chosenCardId the randomly chosen target card, or {@code null} before the trigger is put on the stack
 */
public record ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect(UUID chosenCardId)
        implements CardEffect {

    public ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect() {
        this(null);
    }
}
