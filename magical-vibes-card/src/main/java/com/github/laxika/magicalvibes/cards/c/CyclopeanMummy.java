package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;

@CardRegistration(set = "4ED", collectorNumber = "128")
public class CyclopeanMummy extends Card {

    public CyclopeanMummy() {
        // When this creature dies, exile it.
        addEffect(EffectSlot.ON_DEATH, new ExileSourceCardFromGraveyardEffect());
    }
}
