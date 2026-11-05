package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "99")
@CardRegistration(set = "M11", collectorNumber = "100")
@CardRegistration(set = "DDD", collectorNumber = "43")
@CardRegistration(set = "GVL", collectorNumber = "43")
public class HowlingBanshee extends Card {

    public HowlingBanshee() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                new LoseLifeEffect(3));
    }
}
