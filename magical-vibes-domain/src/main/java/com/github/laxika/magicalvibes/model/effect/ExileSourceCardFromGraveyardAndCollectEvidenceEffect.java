package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the dying source card, then collects evidence from its controller's graveyard.
 *
 * <p>The source card is removed before the evidence choice so it cannot be selected as evidence.
 * If the evidence threshold cannot be met, the effect does nothing and leaves the source card in
 * its graveyard.
 */
public record ExileSourceCardFromGraveyardAndCollectEvidenceEffect(
        int minimumManaValue, boolean returnTapped) implements CardEffect {

    public ExileSourceCardFromGraveyardAndCollectEvidenceEffect {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("minimumManaValue cannot be negative");
        }
    }
}
