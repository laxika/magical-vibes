package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "76")
public class MephiticVapors extends Card {

    public MephiticVapors() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
    }
}
