package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "104")
public class AssaultIntercessor extends Card {

    public AssaultIntercessor() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
