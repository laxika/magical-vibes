package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReduceDashCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "168")
public class Warbringer extends Card {

    public Warbringer() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        addEffect(EffectSlot.STATIC, new ReduceDashCostEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
    }
}
