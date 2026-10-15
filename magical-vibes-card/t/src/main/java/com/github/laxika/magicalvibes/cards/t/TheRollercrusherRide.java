package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "155")
public class TheRollercrusherRide extends Card {

    public TheRollercrusherRide() {
        targetUpTo(new XValue(), TargetFilters.creature(), 100)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDamageToEachTargetEffect(new XValue()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new DoubleControllerDamageEffect(null, false)));
    }
}
