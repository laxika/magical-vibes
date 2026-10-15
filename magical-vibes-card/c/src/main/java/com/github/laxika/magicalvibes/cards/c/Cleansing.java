package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyLandsUnlessAnyPlayerPaysLifeEffect;

@CardRegistration(set = "DRK", collectorNumber = "4")
public class Cleansing extends Card {

    public Cleansing() {
        addEffect(EffectSlot.SPELL, new DestroyLandsUnlessAnyPlayerPaysLifeEffect(1));
    }
}
