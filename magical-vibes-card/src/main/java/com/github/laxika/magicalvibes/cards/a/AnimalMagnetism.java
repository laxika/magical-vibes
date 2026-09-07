package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimalMagnetismEffect;

@CardRegistration(set = "ONS", collectorNumber = "245")
public class AnimalMagnetism extends Card {

    public AnimalMagnetism() {
        addEffect(EffectSlot.SPELL, new AnimalMagnetismEffect());
    }
}
