package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "137")
public class MagmakinArtillerist extends Card {

    public MagmakinArtillerist() {
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                new DealDamageToPlayersEffect(new EventValue(), Subjects.eachOpponent()));
        addTrigger(Triggers.youCycleThisCard(),
                new DealDamageToPlayersEffect(1, Subjects.eachOpponent()));
        addCycling("{1}{R}");
    }
}
