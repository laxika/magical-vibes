package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "53")
public class ThermalGlider extends Card {

    public ThermalGlider() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));
    }
}
