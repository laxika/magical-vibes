package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Shimatsu the Bloodcloaked — {3}{R} Legendary Creature — Demon Spirit (0/0).
 * <p>
 * "As Shimatsu the Bloodcloaked enters, sacrifice any number of permanents. Shimatsu the Bloodcloaked
 * enters with that many +1/+1 counters on it."
 */
@CardRegistration(set = "CHK", collectorNumber = "186")
public class ShimatsuTheBloodcloaked extends Card {

    public ShimatsuTheBloodcloaked() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsAsEntersForCountersEffect(new PermanentTruePredicate(), 1));
    }
}
