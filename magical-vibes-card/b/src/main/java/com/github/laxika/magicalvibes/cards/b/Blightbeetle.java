package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "87")
public class Blightbeetle extends Card {

    public Blightbeetle() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
        addStaticEffect(new CantHaveCountersEffect(Subjects.creaturesYourOpponentsControl(),
                CounterType.PLUS_ONE_PLUS_ONE));
    }
}
