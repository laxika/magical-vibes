package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDC", collectorNumber = "113")
public class MarchesasDecree extends Card {

    public MarchesasDecree() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
