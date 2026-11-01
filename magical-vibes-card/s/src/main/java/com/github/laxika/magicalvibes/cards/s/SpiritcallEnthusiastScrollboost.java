package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "33")
public class SpiritcallEnthusiastScrollboost extends Card {

    public SpiritcallEnthusiastScrollboost() {
        setBackFaceCard(new Scrollboost());

        // Whenever one or more tokens you control enter, this creature becomes prepared.
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Scrollboost";
    }
}
