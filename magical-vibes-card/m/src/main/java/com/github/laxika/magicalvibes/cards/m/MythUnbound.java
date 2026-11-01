package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCommanderCastCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "32")
public class MythUnbound extends Card {

    public MythUnbound() {
        addStaticEffect(new ReduceCommanderCastCostEffect(new CommanderCastsFromCommandZoneThisGame()));
        addTrigger(Triggers.putIntoCommandZone(null, ObjectRelation.YOU_OWN), new DrawCardEffect(1));
    }
}
