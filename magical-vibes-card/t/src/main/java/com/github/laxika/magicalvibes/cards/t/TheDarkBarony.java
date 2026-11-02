package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "6")
@CardRegistration(set = "OPCA", collectorNumber = "19")
public class TheDarkBarony extends Card {

    public TheDarkBarony() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.ANOTHER),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addTrigger(Triggers.chaosEnsues(),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
