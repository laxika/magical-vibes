package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "DTK", collectorNumber = "12")
public class DromokaCaptain extends Card {

    public DromokaCaptain() {
        // Whenever this creature attacks, bolster 1.
        addEffect(EffectSlot.ON_ATTACK, new BolsterEffect(1));
    }
}
