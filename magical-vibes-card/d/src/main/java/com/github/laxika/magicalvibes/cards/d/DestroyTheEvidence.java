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
        // Destroy target land, then its controller reveals until a land and mills those cards. The
        // mill resolves the controller from the destroyed land's captured controller.
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new RevealUntilLandsMillTargetPlayerEffect(1, MillRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
