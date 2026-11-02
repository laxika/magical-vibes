package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "153")
public class CliffhavenVampire extends Card {

    public CliffhavenVampire() {
        addTrigger(Triggers.youGainLife(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
