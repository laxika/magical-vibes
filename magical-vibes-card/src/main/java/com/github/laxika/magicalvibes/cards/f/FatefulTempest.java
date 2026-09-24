package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FatefulTempestEffect;

@CardRegistration(set = "SOC", collectorNumber = "31")
@CardRegistration(set = "SOC", collectorNumber = "79")
public class FatefulTempest extends Card {

    public FatefulTempest() {
        addEffect(EffectSlot.SPELL, new FatefulTempestEffect());
    }
}
