package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "107")
@CardRegistration(set = "PIP", collectorNumber = "635")
public class LegateLaniusCaesarsAce extends Card {

    public LegateLaniusCaesarsAce() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsEffect(
                        new Divided(
                                new Sum(new PermanentCount(creature, CountScope.CONTROLLER), new Fixed(9)),
                                10),
                        new PermanentAnyOfPredicate(List.of(creature)), SacrificeRecipient.EACH_OPPONENT,
                        true));
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(creature,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
