package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for Gift of Immortality: return the enchanted creature under its owner's control,
 * then schedule the source Aura to return attached to that returned permanent at the next end step.
 */
public record ReturnEnchantedCreatureAndReattachAuraOnDeathEffect(
        UUID dyingCreatureCardId
) implements CardEffect {

    /** Card-definition constructor; the dying creature ID is bound by the trigger collector. */
    public ReturnEnchantedCreatureAndReattachAuraOnDeathEffect() {
        this(null);
    }
}
