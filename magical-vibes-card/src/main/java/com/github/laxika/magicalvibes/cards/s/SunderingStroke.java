package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "ELD", collectorNumber = "144")
public class SunderingStroke extends Card {

    public SunderingStroke() {
        ColorSpentToCast threshold = new ColorSpentToCast(ManaColor.RED, 7);

        target(1, 3)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(threshold), DealDividedDamageEffect.chosenAmongAnyTargets(7)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        threshold, new DealDamageToEachTargetEffect(new Fixed(7))));
    }
}
