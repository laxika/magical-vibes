package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "31")
public class PairedTactician extends Card {

    public PairedTactician() {
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.atTriggerTime(
                new HasAttacker(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
