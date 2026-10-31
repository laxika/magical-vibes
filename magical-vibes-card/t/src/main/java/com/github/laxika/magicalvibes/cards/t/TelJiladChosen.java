package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "132")
public class TelJiladChosen extends Card {

    public TelJiladChosen() {
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.ARTIFACT)));
    }
}
