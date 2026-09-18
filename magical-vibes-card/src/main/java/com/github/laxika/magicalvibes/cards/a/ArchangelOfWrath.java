package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "3")
public class ArchangelOfWrath extends Card {

    public ArchangelOfWrath() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{B}"));
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.singlePayment(List.of("{R}")));

        targetWhenKicked(null, 0, 0, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new DealDamageToAnyTargetEffect(2)));

        targetWithDynamicCount(new RepeatedAdditionalCostCount("{R}"), null, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new RepeatedAdditionalCostPaid("{R}"),
                                new DealDamageToAnyTargetEffect(2)));
    }
}
