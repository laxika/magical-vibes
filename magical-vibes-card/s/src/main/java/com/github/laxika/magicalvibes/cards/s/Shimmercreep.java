package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "120")
public class Shimmercreep extends Card {

    public Shimmercreep() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new LoseLifeEffect(new ColorsAmongControlledPermanents(), LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(new ColorsAmongControlledPermanents())
        ));
    }
}
