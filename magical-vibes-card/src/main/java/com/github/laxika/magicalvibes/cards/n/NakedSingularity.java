package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.NakedSingularityManaEffect;

@CardRegistration(set = "ICE", collectorNumber = "330")
public class NakedSingularity extends Card {

    public NakedSingularity() {
        // Cumulative upkeep {3}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{3}"));

        // If tapped for mana, Plains produce {R}, Islands produce {G}, Swamps produce {W},
        // Mountains produce {U}, and Forests produce {B} instead of any other type.
        addEffect(EffectSlot.STATIC, new NakedSingularityManaEffect());
    }
}
