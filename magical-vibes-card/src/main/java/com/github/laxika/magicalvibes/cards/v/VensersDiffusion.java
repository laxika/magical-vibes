package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "FUT", collectorNumber = "47")
public class VensersDiffusion extends Card {

    public VensersDiffusion() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.targetNonlandPermanentOrSuspendedCard());
    }
}
