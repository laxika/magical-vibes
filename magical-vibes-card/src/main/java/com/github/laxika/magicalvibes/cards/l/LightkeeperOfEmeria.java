package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "12")
public class LightkeeperOfEmeria extends Card {

    public LightkeeperOfEmeria() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{W}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(
                new Scaled(new RepeatedAdditionalCostCount("{W}"), 2)));
    }
}
