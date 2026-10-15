package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DTK", collectorNumber = "69")
public class PalaceFamiliar extends Card {

    public PalaceFamiliar() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
