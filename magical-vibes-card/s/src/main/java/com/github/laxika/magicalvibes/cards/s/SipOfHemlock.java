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
        // Destruction happens first; the life loss then reads the controller remembered from the destroyed target.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
