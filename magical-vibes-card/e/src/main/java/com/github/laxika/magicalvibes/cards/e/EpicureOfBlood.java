package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "95")
@CardRegistration(set = "M20", collectorNumber = "99")
@CardRegistration(set = "C21", collectorNumber = "143")
public class EpicureOfBlood extends Card {

    public EpicureOfBlood() {
        addTrigger(Triggers.youGainLife(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
