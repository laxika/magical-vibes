package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "AVR", collectorNumber = "48")
public class DevastationTide extends Card {

    public DevastationTide() {
        // Miracle {1}{U}
        addCastingOption(new MiracleCast("{1}{U}"));

        // Return all nonland permanents to their owners' hands.
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
