package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DKA", collectorNumber = "137")
public class DrogskolReaver extends Card {

    public DrogskolReaver() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new DrawCardEffect(1));
    }
}
