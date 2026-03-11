package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record BoostSelfPerControlledSubtypeEffect(
        CardSubtype subtype,
        int powerPerPermanent,
        int toughnessPerPermanent
) implements CardEffect {
}
