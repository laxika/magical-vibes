package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives each creature controlled by the effect's controller every modeled protection ability
 * currently held by a creature that controller controls, until end of turn.
 *
 * <p>The ability set is snapshotted by the normal-effect handler when this resolves. The effect
 * exists separately from the fixed-color protection grants because protection can carry different
 * parameters, such as card types, subtypes, or a mana-value threshold.
 */
public record GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect() implements CardEffect {
}
