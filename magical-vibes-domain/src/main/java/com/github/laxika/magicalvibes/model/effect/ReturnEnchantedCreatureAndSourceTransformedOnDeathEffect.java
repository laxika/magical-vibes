package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns the creature enchanted by the source Aura to the Aura controller's battlefield, then
 * returns the source Aura from its graveyard to the battlefield transformed under that controller's
 * control.
 */
public record ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect(
        UUID dyingCreatureCardId
) implements CardEffect {

    /** Card-definition constructor; the dying creature ID is bound when the trigger is collected. */
    public ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect() {
        this(null);
    }
}
