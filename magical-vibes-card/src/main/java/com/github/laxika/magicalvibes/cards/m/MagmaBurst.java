package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PLS", collectorNumber = "66")
public class MagmaBurst extends Card {

    public MagmaBurst() {
        addEffect(EffectSlot.STATIC, new KickerEffect(2, new PermanentIsLandPredicate(), "two lands"));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        targetWhenKicked(null, 0, 0, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                        DealDamageToAnyTargetEffect.forTargetGroup(3, 0)));
    }
}
