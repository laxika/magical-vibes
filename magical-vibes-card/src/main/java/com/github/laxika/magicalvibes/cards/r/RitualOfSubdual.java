package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;

@CardRegistration(set = "ICE", collectorNumber = "261")
public class RitualOfSubdual extends Card {

    public RitualOfSubdual() {
        // Cumulative upkeep {2}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{2}"));

        // If a land is tapped for mana, it produces colorless mana instead of any other type.
        addEffect(EffectSlot.STATIC, new ReplaceLandManaWithColorEffect(ManaColor.COLORLESS));
    }
}
