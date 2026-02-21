package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;

@CardRegistration(set = "10E", collectorNumber = "306")
public class Upwelling extends Card {

    public Upwelling() {
        addEffect(EffectSlot.STATIC, new PreventManaDrainEffect());
    }
}
