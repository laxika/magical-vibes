package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "178")
@CardRegistration(set = "ZNR", collectorNumber = "112")
public class MaraudingBlightPriest extends Card {

    public MaraudingBlightPriest() {
        addTrigger(Triggers.youGainLife(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
