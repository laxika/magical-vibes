package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOI", collectorNumber = "196")
public class BywayCourier extends Card {

    public BywayCourier() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofClueToken(1));
    }
}
