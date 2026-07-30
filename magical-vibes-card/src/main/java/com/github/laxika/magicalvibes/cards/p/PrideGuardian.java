package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M12", collectorNumber = "31")
public class PrideGuardian extends Card {

    public PrideGuardian() {
        addEffect(EffectSlot.ON_BLOCK, new GainLifeEffect(3));
    }
}
