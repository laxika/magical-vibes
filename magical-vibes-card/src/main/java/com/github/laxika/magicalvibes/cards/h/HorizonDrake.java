package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "30")
public class HorizonDrake extends Card {

    public HorizonDrake() {
        addEffect(EffectSlot.STATIC, new ProtectionFromCardTypesEffect(Set.of(CardType.LAND)));
    }
}
