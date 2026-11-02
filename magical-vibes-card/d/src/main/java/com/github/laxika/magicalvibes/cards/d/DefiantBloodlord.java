package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "107")
@CardRegistration(set = "C21", collectorNumber = "142")
public class DefiantBloodlord extends Card {

    public DefiantBloodlord() {
        addTrigger(Triggers.youGainLife(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));
    }
}
