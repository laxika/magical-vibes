package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ARB", collectorNumber = "110")
public class SphinxOfTheSteelWind extends Card {

    public SphinxOfTheSteelWind() {
        // Flying, first strike, vigilance, lifelink are auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED, CardColor.GREEN)));
    }
}
