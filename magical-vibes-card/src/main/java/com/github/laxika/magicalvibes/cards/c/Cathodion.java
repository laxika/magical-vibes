package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "MRD", collectorNumber = "149")
public class Cathodion extends Card {

    public Cathodion() {
        addEffect(EffectSlot.ON_DEATH, new AwardManaEffect(ManaColor.COLORLESS, 3));
    }
}
