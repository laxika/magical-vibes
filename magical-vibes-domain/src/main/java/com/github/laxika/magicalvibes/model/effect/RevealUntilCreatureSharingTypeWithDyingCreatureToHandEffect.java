package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Reveals until a creature card sharing a creature type with the creature that died is found,
 * putting that card into its controller's hand and the other revealed cards on the bottom of the
 * library in a random order.
 *
 * @param dyingCreature last-known snapshot of the creature that caused the trigger
 */
public record RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect(Permanent dyingCreature)
        implements CardEffect, DyingCreaturePermanentAwareEffect {

    public RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCreature(Permanent dyingCreature) {
        return new RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect(
                dyingCreature == null ? null : new Permanent(dyingCreature));
    }
}
