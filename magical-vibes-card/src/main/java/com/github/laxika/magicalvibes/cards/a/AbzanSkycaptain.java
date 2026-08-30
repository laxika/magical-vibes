package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "FRF", collectorNumber = "4")
public class AbzanSkycaptain extends Card {

    public AbzanSkycaptain() {
        addEffect(EffectSlot.ON_DEATH, new BolsterEffect(2));
    }
}
