package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "8ED", collectorNumber = "68")
@CardRegistration(set = "ODY", collectorNumber = "78")
@CardRegistration(set = "PC2", collectorNumber = "16")
@CardRegistration(set = "DDT", collectorNumber = "4")
@CardRegistration(set = "E02", collectorNumber = "9")
@CardRegistration(set = "PCA", collectorNumber = "16")
@CardRegistration(set = "C14", collectorNumber = "103")
public class Concentrate extends Card {

    public Concentrate() {
        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
