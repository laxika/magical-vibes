package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "105")
public class AzoriusFirstWing extends Card {

    public AzoriusFirstWing() {
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
