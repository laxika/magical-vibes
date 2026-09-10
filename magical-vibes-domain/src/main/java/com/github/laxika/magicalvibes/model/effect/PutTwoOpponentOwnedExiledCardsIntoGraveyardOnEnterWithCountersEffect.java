package com.github.laxika.magicalvibes.model.effect;

/**
 * As the source creature enters, its controller may put two face-up cards owned by opponents
 * from exile into their owners' graveyards. If they do, the creature enters with the configured
 * number of +1/+1 counters.
 */
public record PutTwoOpponentOwnedExiledCardsIntoGraveyardOnEnterWithCountersEffect(int counterCount)
        implements AsEntersOpponentExileToGraveyardEffect {
}
