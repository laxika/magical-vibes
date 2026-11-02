package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostRandomCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "23")
public class GoldenSidekick extends Card {

    public GoldenSidekick() {
        addTrigger(Triggers.youGainLife(),
                new PerpetuallyBoostRandomCreatureCardInHandEffect(new EventValue()));
    }
}
