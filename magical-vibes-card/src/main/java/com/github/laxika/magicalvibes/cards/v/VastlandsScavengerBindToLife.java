package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BindToLife;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Vastlands Scavenger // Bind to Life (SOS 166). */
@CardRegistration(set = "SOS", collectorNumber = "166")
public class VastlandsScavengerBindToLife extends Card {

    public VastlandsScavengerBindToLife() {
        setBackFaceCard(new BindToLife());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "BindToLife";
    }
}
