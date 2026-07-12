package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;

@CardRegistration(set = "8ED", collectorNumber = "65")
public class Catalog extends Card {

    public Catalog() {
        addEffect(EffectSlot.SPELL, new DrawAndDiscardCardEffect(2, 1));
    }
}
