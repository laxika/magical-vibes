package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "RIX", collectorNumber = "144")
public class Polyraptor extends Card {

    public Polyraptor() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new CreateTokenCopyOfSourceEffect());
    }
}
