package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "ODY", collectorNumber = "128")
public class Decompose extends Card {

    public Decompose() {
        addEffect(EffectSlot.SPELL, new ExileCardsFromGraveyardEffect(3, 0));
    }
}
