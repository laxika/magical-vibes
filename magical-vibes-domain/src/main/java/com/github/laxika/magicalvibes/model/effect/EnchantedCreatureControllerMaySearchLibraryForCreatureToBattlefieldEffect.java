package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an Aura trigger that lets the enchanted creature's controller search their library
 * for a creature card and put it onto the battlefield.
 *
 * <p>The trigger collector routes the optional search to the dying creature's controller, who may
 * differ from the Aura's controller.</p>
 */
public record EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect()
        implements CardEffect {
}
