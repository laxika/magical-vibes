package com.github.laxika.magicalvibes.cards.t;



import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "153")

public class TrueheartTwins extends Card {

    public TrueheartTwins() {
        addStaticEffect(new ExertAsItAttacksEffect());

        addTrigger(Triggers.youExertCreature(), new BoostAllOwnCreaturesEffect(1, 0));
    }
}
