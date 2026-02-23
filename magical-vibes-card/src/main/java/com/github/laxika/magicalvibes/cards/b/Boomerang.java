package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "70")
public class Boomerang extends Card {

    public Boomerang() {
        addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
