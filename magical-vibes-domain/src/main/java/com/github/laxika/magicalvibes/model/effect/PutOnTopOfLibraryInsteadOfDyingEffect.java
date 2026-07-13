package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect (CR 614): "If [this creature] would die, put it on top of
 * its owner's library instead."
 *
 * <p>Checked in {@code GraveyardService.addCardToGraveyard()} when the card would
 * be put into the graveyard from the battlefield (i.e. "die"). When matched, the
 * card is placed on top of its owner's library instead. Used by Gravebane Zombie.</p>
 */
public record PutOnTopOfLibraryInsteadOfDyingEffect() implements CardEffect {
}
