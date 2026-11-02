package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsSuspected;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectChosenOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "126")
@CardRegistration(set = "MKM", collectorNumber = "347")
public class FranticScapegoat extends Card {

    public FranticScapegoat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SuspectEffect(GrantScope.SELF));
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new ConditionalEffect(
                new SourceIsSuspected(),
                new MayEffect(new SuspectChosenOtherCreatureEffect(),
                        "Suspect another creature and stop suspecting this creature?")));
    }
}
