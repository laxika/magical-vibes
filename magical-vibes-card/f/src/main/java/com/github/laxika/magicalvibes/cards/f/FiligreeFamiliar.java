package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "KLD", collectorNumber = "212")
@CardRegistration(set = "KLR", collectorNumber = "239")
@CardRegistration(set = "DDU", collectorNumber = "53")
@CardRegistration(set = "GNT", collectorNumber = "52")
public class FiligreeFamiliar extends Card {

    public FiligreeFamiliar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
