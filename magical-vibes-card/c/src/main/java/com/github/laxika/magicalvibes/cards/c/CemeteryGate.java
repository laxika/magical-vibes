package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "44a")
@CardRegistration(set = "HML", collectorNumber = "44b")
public class CemeteryGate extends Card {

    public CemeteryGate() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
    }
}
