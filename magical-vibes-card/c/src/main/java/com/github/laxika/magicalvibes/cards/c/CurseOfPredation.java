package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IMA", collectorNumber = "159")
@CardRegistration(set = "C13", collectorNumber = "140")
@CardRegistration(set = "CMA", collectorNumber = "99")
public class CurseOfPredation extends Card {

    public CurseOfPredation() {
        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentIsAttackingEnchantedPlayerPredicate()),
                new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsTriggeringPermanentPredicate(), EachPermanentScope.ALL_PLAYERS));
    }
}
