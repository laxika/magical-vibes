package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "73")
public class GreatFierceBee extends Card {

    public GreatFierceBee() {
        // Whenever one or more other creatures die, scry 1.
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANOTHER)), TriggerOptions.ONE_OR_MORE,
                new ScryEffect(1));
    }
}
