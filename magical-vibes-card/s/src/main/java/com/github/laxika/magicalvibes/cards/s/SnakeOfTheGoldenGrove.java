package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "141")
public class SnakeOfTheGoldenGrove extends Card {

    public SnakeOfTheGoldenGrove() {
        addStaticEffect(new TributeEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TributeNotPaidEffect(new GainLifeEffect(4)));
    }
}
