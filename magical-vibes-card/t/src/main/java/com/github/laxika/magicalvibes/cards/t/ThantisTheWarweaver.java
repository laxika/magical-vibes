package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "46")
public class ThantisTheWarweaver extends Card {

    public ThantisTheWarweaver() {
        addStaticEffect(new MatchingCreaturesMustAttackEffect(
                new PermanentIsCreaturePredicate()));
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
