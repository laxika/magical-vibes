package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "193")
public class HostOfTheHereafter extends Card {

    public HostOfTheHereafter() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        var deathTrigger = target(TargetFilters.creatureYouControl(), 0, 1);
        // Whenever this creature or another creature you control dies, if it had counters on it, ...
        // "If it had counters on it" is about whichever creature died, this one included: one ability with two trigger
        // conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        deathTrigger.addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new MoveDyingSourceCountersToTargetCreatureEffect(true)))));
    }
}
