package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record BoostCreaturePerControlledCardTypeEffect(
        CardType cardType,
        int powerPerMatch,
        int toughnessPerMatch,
        GrantScope scope
) implements CardEffect {
}
