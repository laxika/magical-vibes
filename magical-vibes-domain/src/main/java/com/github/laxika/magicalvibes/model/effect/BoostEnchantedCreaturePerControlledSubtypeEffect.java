package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record BoostEnchantedCreaturePerControlledSubtypeEffect(
        CardSubtype subtype,
        int powerPerSubtype,
        int toughnessPerSubtype
) implements CardEffect {
}
