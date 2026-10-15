package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "ODY", collectorNumber = "221")
public class ShowerOfCoals extends Card {

    public ShowerOfCoals() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);

        target(0, 3)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(threshold), new DealDamageToEachTargetEffect(new Fixed(2))))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        threshold, new DealDamageToEachTargetEffect(new Fixed(4))));
    }
}
