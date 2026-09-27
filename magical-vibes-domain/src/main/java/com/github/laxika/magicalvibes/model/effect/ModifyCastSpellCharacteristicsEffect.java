package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/** Records characteristic changes that apply to a permanent spell as it enters the battlefield. */
public record ModifyCastSpellCharacteristicsEffect(
        CardColor additionalColor,
        CardSubtype additionalSubtype,
        Integer basePower,
        Integer baseToughness
) implements CardEffect {
}
