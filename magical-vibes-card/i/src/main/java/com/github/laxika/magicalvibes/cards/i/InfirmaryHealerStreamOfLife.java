package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.StreamOfLife;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Infirmary Healer // Stream of Life (SOS 152).
 */
@CardRegistration(set = "SOS", collectorNumber = "152")
public class InfirmaryHealerStreamOfLife extends Card {

    public InfirmaryHealerStreamOfLife() {
        setBackFaceCard(new StreamOfLife());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "StreamOfLife";
    }
}
