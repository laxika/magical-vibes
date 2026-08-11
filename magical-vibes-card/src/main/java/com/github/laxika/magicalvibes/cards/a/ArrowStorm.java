package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "KTK", collectorNumber = "98")
public class ArrowStorm extends Card {

    public ArrowStorm() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Raid(),
                new DealDamageToAnyTargetEffect(4),
                new DealDamageToAnyTargetEffect(new Fixed(5), new Raid())
        ));
    }
}
