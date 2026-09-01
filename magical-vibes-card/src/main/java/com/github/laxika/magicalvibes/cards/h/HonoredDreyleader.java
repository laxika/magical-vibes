package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "178")
public class HonoredDreyleader extends Card {

    public HonoredDreyleader() {
        PermanentPredicate squirrelOrFood = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL),
                new PermanentHasSubtypePredicate(CardSubtype.FOOD)));

        // When this creature enters, put a +1/+1 counter on it for each other Squirrel and/or Food you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(squirrelOrFood, CountScope.CONTROLLER, true),
                new PermanentIsSourceCardPredicate(),
                EachPermanentScope.ALL_PLAYERS));

        // Whenever another Squirrel or Food you control enters, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                squirrelOrFood,
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
