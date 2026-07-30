package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;

@CardRegistration(set = "M13", collectorNumber = "29")
public class RhoxFaithmender extends Card {

    public RhoxFaithmender() {
        // Lifelink is loaded automatically from Scryfall.
        // If you would gain life, you gain twice that much life instead.
        addEffect(EffectSlot.STATIC, new DoubleLifeGainEffect());
    }
}
