package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M12", collectorNumber = "160")
public class WarstormSurge extends Card {

    public WarstormSurge() {
        // Whenever a creature you control enters, it deals damage equal to its power to any target.
        // The entering creature is the damage source, so its power is read at resolution.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
