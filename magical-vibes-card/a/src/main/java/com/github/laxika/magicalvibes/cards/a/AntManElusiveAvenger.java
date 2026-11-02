package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "73")
@CardRegistration(set = "MSC", collectorNumber = "390")
public class AntManElusiveAvenger extends Card {

    public AntManElusiveAvenger() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate())));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
