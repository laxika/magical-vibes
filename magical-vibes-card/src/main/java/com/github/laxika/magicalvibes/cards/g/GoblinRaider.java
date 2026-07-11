package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "P02", collectorNumber = "103")
public class GoblinRaider extends Card {

    public GoblinRaider() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
