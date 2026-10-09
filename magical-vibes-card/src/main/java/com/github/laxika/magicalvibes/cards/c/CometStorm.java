package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "76")
@CardRegistration(set = "MM2", collectorNumber = "111")
@CardRegistration(set = "CMD", collectorNumber = "117")
@CardRegistration(set = "C15", collectorNumber = "146")
@CardRegistration(set = "C20", collectorNumber = "148")
@CardRegistration(set = "C17", collectorNumber = "132")
@CardRegistration(set = "CMA", collectorNumber = "79")
public class CometStorm extends Card {

    public CometStorm() {
        setAdditionalCostPerExtraTarget(1);
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{1}")));
        target(1, Integer.MAX_VALUE).addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new XValue()));
    }
}
