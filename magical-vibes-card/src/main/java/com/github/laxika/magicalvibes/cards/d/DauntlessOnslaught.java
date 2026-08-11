package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "6")
public class DauntlessOnslaught extends Card {

    public DauntlessOnslaught() {
        // Up to two target creatures each get +2/+2 until end of turn.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
