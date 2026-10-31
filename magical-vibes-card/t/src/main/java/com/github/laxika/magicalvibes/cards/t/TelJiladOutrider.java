package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "87")
public class TelJiladOutrider extends Card {

    public TelJiladOutrider() {
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.ARTIFACT)));
    }
}
