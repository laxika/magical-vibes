package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;

@CardRegistration(set = "HOC", collectorNumber = "24")
@CardRegistration(set = "HOC", collectorNumber = "64")
@CardRegistration(set = "LTC", collectorNumber = "35")
@CardRegistration(set = "LTC", collectorNumber = "118")
public class ArwenWeaverOfHope extends Card {

    public ArwenWeaverOfHope() {
        // Each other creature you control enters with additional +1/+1 counters equal to Arwen's toughness.
        addEffect(EffectSlot.STATIC,
                ControlledCreaturesEnterWithAdditionalCountersEffect.forAllCreatures(new SourceToughness()));
    }
}
