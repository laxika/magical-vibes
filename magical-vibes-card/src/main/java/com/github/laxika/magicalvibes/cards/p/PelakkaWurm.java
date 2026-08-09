package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M19", collectorNumber = "192")
public class PelakkaWurm extends Card {

    public PelakkaWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(7));
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
