package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "150")
@CardRegistration(set = "MMA", collectorNumber = "203")
@CardRegistration(set = "MPS", collectorNumber = "33")
@CardRegistration(set = "A25", collectorNumber = "222")
@CardRegistration(set = "HA6", collectorNumber = "9")
@CardRegistration(set = "TSR", collectorNumber = "390")
@CardRegistration(set = "LCC", collectorNumber = "105")
public class ChaliceOfTheVoid extends Card {

    public ChaliceOfTheVoid() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));
        // Whenever a player casts a spell with mana value equal to the number of charge counters on this artifact,
        // counter that spell. The counters matter only as the spell is cast (a trigger condition, not an "if").
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.CHARGE)),
                new CounterSpellEffect());
    }
}
