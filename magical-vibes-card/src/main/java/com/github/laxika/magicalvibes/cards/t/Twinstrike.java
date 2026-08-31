package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "134")
public class Twinstrike extends Card {

    public Twinstrike() {
        target(TargetFilters.creature(), 2, 2).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                new DealDamageToEachTargetEffect(new Fixed(2)),
                new DestroyEachTargetPermanentEffect()
        ));
    }
}
