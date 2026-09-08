package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "M21", collectorNumber = "55")
public class LibraryLarcenist extends Card {

    public LibraryLarcenist() {
        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect(1));
    }
}
