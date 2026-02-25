package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record GainLifeOnSpellCastEffect(CardPredicate spellFilter, int amount) implements CardEffect {
}
