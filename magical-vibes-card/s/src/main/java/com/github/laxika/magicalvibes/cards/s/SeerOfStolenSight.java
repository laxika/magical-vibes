package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "330")
public class SeerOfStolenSight extends Card {

    public SeerOfStolenSight() {
        addTrigger(new TriggerCondition(Triggers.dies(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new SurveilEffect(1));
    }
}
