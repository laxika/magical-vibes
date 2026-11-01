package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "175")
public class CanopyStalker extends Card {

    public CanopyStalker() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new GainLifeEffect(new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
