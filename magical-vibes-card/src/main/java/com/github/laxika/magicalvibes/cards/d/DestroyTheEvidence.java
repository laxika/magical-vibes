package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "64")
public class DestroyTheEvidence extends Card {

    public DestroyTheEvidence() {
        // The reveal-and-mill runs first so the targeted land is still on the battlefield when its
        // controller is derived; the destruction cannot change who reveals, so the outcome matches
        // the printed order.
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new RevealUntilLandsMillTargetPlayerEffect(1, MillRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
