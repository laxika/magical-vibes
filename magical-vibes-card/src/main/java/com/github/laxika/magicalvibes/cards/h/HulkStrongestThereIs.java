package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "724")
public class HulkStrongestThereIs extends Card {

    public HulkStrongestThereIs() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DoublePlusOneCountersOnControlledCreaturesEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GAMMA)));
    }
}
