package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ODY", collectorNumber = "113")
public class Upheaval extends Card {

    public Upheaval() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(null));
    }
}
