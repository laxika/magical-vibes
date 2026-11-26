package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "PIP", collectorNumber = "59")
@CardRegistration(set = "PIP", collectorNumber = "587")
public class IanTheReckless extends Card {

    public IanTheReckless() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceMatchesPermanentPredicate(new PermanentIsModifiedPredicate())),
                new MayEffect(
                        SequenceEffect.of(
                                DealDamageEffect.to(Subjects.you(), new SourcePower()),
                                DealDamageEffect.toAnyTarget(new SourcePower())),
                        "Have Ian the Reckless deal damage equal to its power to you and any target?"));
    }
}
