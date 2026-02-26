package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Imprint trigger effect for Mimic Vat: exile a dying creature card from the graveyard,
 * set it as imprinted on the source permanent, and return any previously imprinted card
 * to its owner's graveyard.
 *
 * @param dyingCardId the card ID of the dying creature (null in card definition, filled at trigger time)
 */
public record ImprintDyingCreatureEffect(UUID dyingCardId) implements CardEffect {

    public ImprintDyingCreatureEffect() {
        this(null);
    }
}
