package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "111")
public class TestamentBearer extends Card {

    public TestamentBearer() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(3, 1));
    }
}
