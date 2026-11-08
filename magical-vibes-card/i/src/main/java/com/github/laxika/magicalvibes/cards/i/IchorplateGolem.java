package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "230")
public class IchorplateGolem extends Card {

    public IchorplateGolem() {
        // Whenever a creature you control enters, if it has one or more oil counters on it, put an oil counter on
        // it. The intervening "if" is judged as the creature enters and again as the ability resolves (CR 603.4).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(
                        new PermanentHasCountersPredicate(CounterType.OIL))),
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.OIL, 1));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasCountersPredicate(CounterType.OIL)));
    }
}
