package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOnePlusOneCountersOnAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

@CardRegistration(set = "WOT", collectorNumber = "59")
@CardRegistration(set = "C13", collectorNumber = "162")
public class PrimalVigor extends Card {

    public PrimalVigor() {
        addEffect(EffectSlot.STATIC, MultiplyTokenCreationEffect.forAllPlayers(2));
        addEffect(EffectSlot.STATIC, new DoublePlusOnePlusOneCountersOnAllCreaturesEffect());
    }
}
