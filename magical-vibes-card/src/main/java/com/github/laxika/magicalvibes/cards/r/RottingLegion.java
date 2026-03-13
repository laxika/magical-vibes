package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "M11", collectorNumber = "115")
public class RottingLegion extends Card {

    public RottingLegion() {
        // Rotting Legion enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
