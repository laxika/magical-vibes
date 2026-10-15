package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.RealityTwistManaEffect;

@CardRegistration(set = "ICE", collectorNumber = "94")
public class RealityTwist extends Card {

    public RealityTwist() {
        // Cumulative upkeep {1}{U}{U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}{U}{U}"));

        // If tapped for mana, Plains produce {R}, Swamps produce {G}, Mountains produce {W},
        // and Forests produce {B} instead of any other type.
        addEffect(EffectSlot.STATIC, new RealityTwistManaEffect());
    }
}
