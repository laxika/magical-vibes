package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "60")
public class AleshasVanguard extends Card {

    public AleshasVanguard() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
    }
}
