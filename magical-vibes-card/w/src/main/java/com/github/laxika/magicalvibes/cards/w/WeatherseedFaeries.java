package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "48")
public class WeatherseedFaeries extends Card {

    public WeatherseedFaeries() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));
    }
}
