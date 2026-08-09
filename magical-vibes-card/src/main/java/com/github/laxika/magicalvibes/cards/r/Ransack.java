package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "STH", collectorNumber = "39")
public class Ransack extends Card {

    public Ransack() {
        addEffect(EffectSlot.SPELL, new ScryEffect(5, LibraryOwner.TARGET_PLAYER));
    }
}
