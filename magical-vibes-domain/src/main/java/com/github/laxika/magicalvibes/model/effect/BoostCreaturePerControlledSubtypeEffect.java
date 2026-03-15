package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record BoostCreaturePerControlledSubtypeEffect(
        CardSubtype subtype,
        int powerPerSubtype,
        int toughnessPerSubtype,
        GrantScope scope
) implements CardEffect {
}
