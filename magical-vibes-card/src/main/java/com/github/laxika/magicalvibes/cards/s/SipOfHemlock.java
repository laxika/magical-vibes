package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "106")
public class SipOfHemlock extends Card {

    public SipOfHemlock() {
        // The life loss must resolve before the destruction while the target permanent still exists.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
