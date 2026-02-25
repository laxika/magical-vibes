package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record GainLifeForEachSubtypeOnBattlefieldEffect(CardSubtype subtype) implements CardEffect {
}
