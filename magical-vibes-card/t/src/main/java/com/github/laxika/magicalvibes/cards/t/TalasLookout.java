package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "68")
public class TalasLookout extends Card {

    public TalasLookout() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
    }
}
