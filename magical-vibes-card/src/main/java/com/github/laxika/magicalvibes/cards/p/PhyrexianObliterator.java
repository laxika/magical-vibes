package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;

@CardRegistration(set = "NPH", collectorNumber = "68")
public class PhyrexianObliterator extends Card {

    public PhyrexianObliterator() {
        // Whenever a source deals damage to this creature, that source's controller
        // sacrifices that many permanents.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DamageSourceControllerSacrificesPermanentsEffect(0, null));
    }
}
