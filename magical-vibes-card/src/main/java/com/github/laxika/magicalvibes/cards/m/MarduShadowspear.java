package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "74")
public class MarduShadowspear extends Card {

    public MarduShadowspear() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
