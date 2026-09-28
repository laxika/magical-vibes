package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NEO", collectorNumber = "38")
@CardRegistration(set = "SLD", collectorNumber = "896")
@CardRegistration(set = "MOC", collectorNumber = "208")
public class SpiritedCompanion extends Card {

    public SpiritedCompanion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }
}
