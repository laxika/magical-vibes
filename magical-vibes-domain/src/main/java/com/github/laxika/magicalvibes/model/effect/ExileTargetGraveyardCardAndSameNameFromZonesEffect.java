package com.github.laxika.magicalvibes.model.effect;

/**
 * Targets a card in a graveyard (other than a basic land card), then searches its owner's
 * graveyard, hand, and library for any number of cards with the same name and exiles them.
 * Then that player shuffles their library.
 * <p>
 * Used by: Surgical Extraction
 */
public record ExileTargetGraveyardCardAndSameNameFromZonesEffect() implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }

    @Override
    public boolean canTargetAnyGraveyard() {
        return true;
    }
}
