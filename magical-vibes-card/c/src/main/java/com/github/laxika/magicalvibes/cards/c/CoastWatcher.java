package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "30")
public class CoastWatcher extends Card {

    public CoastWatcher() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
    }
}
