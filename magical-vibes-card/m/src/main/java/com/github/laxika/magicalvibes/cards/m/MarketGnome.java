package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "22")
public class MarketGnome extends Card {

    public MarketGnome() {
        SequenceEffect gainLifeAndDraw = SequenceEffect.of(new GainLifeEffect(1), new DrawCardEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), gainLifeAndDraw);
        addTrigger(Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS), gainLifeAndDraw);
    }
}
