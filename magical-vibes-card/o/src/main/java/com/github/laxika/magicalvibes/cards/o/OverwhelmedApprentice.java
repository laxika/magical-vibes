package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "60")
public class OverwhelmedApprentice extends Card {

    public OverwhelmedApprentice() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
