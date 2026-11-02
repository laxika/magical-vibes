package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "137")
public class MagmakinArtillerist extends Card {

    public MagmakinArtillerist() {
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.youCycleThisCard(),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addCycling("{1}{R}");
    }
}
