package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RoostSeek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "TDM", collectorNumber = "157")
public class SaguWildling extends Card {

    public SaguWildling() {
        setBackFaceCard(new RoostSeek());
        addCastingOption(new OmenCast());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }

    @Override
    public String getBackFaceClassName() {
        return "RoostSeek";
    }
}
