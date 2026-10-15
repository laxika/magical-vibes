package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "84")
public class PrismaticLace extends Card {

    public PrismaticLace() {
        // Target permanent becomes the color or colors of your choice. The controller picks the
        // colors on resolution and they replace the permanent's colors indefinitely (CR 105.3).
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new BecomeChosenColorsIndefinitelyEffect(true));
    }
}
