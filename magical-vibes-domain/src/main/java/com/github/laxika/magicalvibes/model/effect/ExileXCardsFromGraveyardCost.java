package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that requires exiling X cards from the controller's graveyard.
 * The number of exiled cards becomes the X value for the spell (e.g. Harvest Pyre).
 * The player chooses which cards to exile and how many (0 or more).
 */
public record ExileXCardsFromGraveyardCost() implements CostEffect {
}
