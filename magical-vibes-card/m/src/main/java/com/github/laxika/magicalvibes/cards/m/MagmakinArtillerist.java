package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "137")
public class MagmakinArtillerist extends Card {

    public MagmakinArtillerist() {
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                DealDamageEffect.to(Subjects.eachOpponent(), new EventValue()));
        addTrigger(Triggers.youCycleThisCard(),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
        addCycling("{1}{R}");
    }
}
