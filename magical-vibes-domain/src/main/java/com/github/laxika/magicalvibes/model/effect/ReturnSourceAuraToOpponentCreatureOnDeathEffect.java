package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, return this aura from its owner's
 * graveyard to the battlefield attached to a creature controlled by an opponent of the dying
 * creature's controller. Used by Necrotic Plague.
 *
 * <p>The {@code enchantedCreatureControllerId} is baked in at trigger time by
 * {@code DeathTriggerService.checkEnchantedPermanentDeathTriggers()} — it captures the dying
 * creature's controller so the resolution logic knows who chooses the target and whose opponents'
 * creatures are valid targets.</p>
 *
 * @param enchantedCreatureControllerId the controller of the creature that just died; {@code null}
 *                                      in the card definition (baked in at trigger time)
 */
public record ReturnSourceAuraToOpponentCreatureOnDeathEffect(
        UUID enchantedCreatureControllerId
) implements CardEffect {

    /**
     * Card-definition constructor — the controller ID is not yet known.
     */
    public ReturnSourceAuraToOpponentCreatureOnDeathEffect() {
        this((UUID) null);
    }
}
