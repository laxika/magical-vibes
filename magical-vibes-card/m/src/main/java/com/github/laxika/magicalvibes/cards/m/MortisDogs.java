package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "66")
public class MortisDogs extends Card {

    public MortisDogs() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER));
    }
}
