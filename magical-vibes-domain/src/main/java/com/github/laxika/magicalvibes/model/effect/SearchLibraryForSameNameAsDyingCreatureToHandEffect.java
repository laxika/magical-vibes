package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a card with the same name as the creature that died,
 * reveals it, and puts it into the controller's hand.
 *
 * <p>The name is the stack entry's triggering permanent's, as it last existed on the battlefield
 * (CR 603.10a), so the effect still finds it when the dead card has left its graveyard before
 * resolution.</p>
 */
public record SearchLibraryForSameNameAsDyingCreatureToHandEffect() implements CardEffect {
}
