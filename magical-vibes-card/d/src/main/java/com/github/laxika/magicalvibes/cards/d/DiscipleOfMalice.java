package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "139")
public class DiscipleOfMalice extends Card {

    public DiscipleOfMalice() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addCycling("{2}");
    }
}
