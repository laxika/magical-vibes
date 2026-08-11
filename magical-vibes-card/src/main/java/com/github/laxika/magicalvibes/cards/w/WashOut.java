package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfChosenColorToHandEffect;

@CardRegistration(set = "INV", collectorNumber = "87")
public class WashOut extends Card {

    public WashOut() {
        addEffect(EffectSlot.SPELL, new ReturnAllPermanentsOfChosenColorToHandEffect());
    }
}
