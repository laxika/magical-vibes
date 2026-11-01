package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "3")
@CardRegistration(set = "IMA", collectorNumber = "9")
public class AuriokChampion extends Card {

    public AuriokChampion() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
