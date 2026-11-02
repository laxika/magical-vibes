package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "110")
public class MalakirBloodPriest extends Card {

    public MalakirBloodPriest() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new LoseLifeEffect(new PartySize(), LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(new PartySize())));
    }
}
