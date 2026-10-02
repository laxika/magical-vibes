package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Returns a chosen permanent and perpetually upgrades it when it is a non-Angel creature card. */
public record ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect(
        PermanentPredicate filter,
        String permanentDescription
) implements CardEffect {
}
