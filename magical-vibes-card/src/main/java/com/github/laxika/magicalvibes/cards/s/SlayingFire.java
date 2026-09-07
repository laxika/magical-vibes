package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "ELD", collectorNumber = "143")
public class SlayingFire extends Card {

    public SlayingFire() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ColorSpentToCast(ManaColor.RED, 3),
                new DealDamageToAnyTargetEffect(3),
                new DealDamageToAnyTargetEffect(4)));
    }
}
