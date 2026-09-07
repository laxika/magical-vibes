package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "175")
public class AvatarOfTheResolute extends Card {

    public AvatarOfTheResolute() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                        )),
                        CountScope.CONTROLLER,
                        true)));
    }
}
