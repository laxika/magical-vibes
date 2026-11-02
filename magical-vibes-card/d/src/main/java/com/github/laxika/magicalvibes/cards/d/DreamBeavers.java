package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "62")
public class DreamBeavers extends Card {

    public DreamBeavers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));
    }
}
