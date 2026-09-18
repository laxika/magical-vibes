package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "7")
@CardRegistration(set = "STA", collectorNumber = "5")
@CardRegistration(set = "MAR", collectorNumber = "44")
public class Ephemerate extends Card {

    public Ephemerate() {
        // Exile target creature you control, then return it to the battlefield under its owner's control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget());
    }
}
