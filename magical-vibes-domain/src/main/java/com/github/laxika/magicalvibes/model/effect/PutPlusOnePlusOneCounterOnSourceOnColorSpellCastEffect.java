package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

public record PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
        Set<CardColor> triggerColors,
        int amount,
        boolean onlyOwnSpells
) implements CardEffect {
}
