package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Searches the controller's library for a card with the required subtype and puts it
 * onto the battlefield attached to a target player. Used by cards like Bitterheart Witch.
 *
 * @param requiredSubtype the subtype to filter for (e.g. CURSE)
 */
public record SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect(CardSubtype requiredSubtype) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
