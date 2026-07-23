package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;

@CardRegistration(set = "ICE", collectorNumber = "135")
public class InfernalDarkness extends Card {

    public InfernalDarkness() {
        // Cumulative upkeep—Pay {B} and 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{B}", 1));

        // If a land is tapped for mana, it produces {B} instead of any other type.
        addEffect(EffectSlot.STATIC, new ReplaceLandManaWithColorEffect(ManaColor.BLACK));
    }
}
