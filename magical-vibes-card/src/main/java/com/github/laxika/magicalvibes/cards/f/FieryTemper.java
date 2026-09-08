package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "INR", collectorNumber = "154")
@CardRegistration(set = "SOI", collectorNumber = "156")
@CardRegistration(set = "TOR", collectorNumber = "97")
@CardRegistration(set = "TSB", collectorNumber = "62")
public class FieryTemper extends Card {

    public FieryTemper() {
        // Fiery Temper deals 3 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        // Madness {R}
        addCastingOption(new MadnessCast("{R}"));
    }
}
