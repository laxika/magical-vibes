package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record GainLifeOnColorSpellCastEffect(CardColor triggerColor, int amount) implements CardEffect {
}
