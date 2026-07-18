package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayShuffleEffect;

@CardRegistration(set = "4ED", collectorNumber = "54")
public class Visions extends Card {

    public Visions() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsOfTargetLibraryMayShuffleEffect(5));
    }
}
