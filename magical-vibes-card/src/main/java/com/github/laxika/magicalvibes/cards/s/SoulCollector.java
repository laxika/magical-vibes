package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingDamagedCreatureUnderControlEffect;

@CardRegistration(set = "TSB", collectorNumber = "47")
public class SoulCollector extends Card {

    public SoulCollector() {
        addMorph("{B}{B}{B}");
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new ReturnDyingDamagedCreatureUnderControlEffect(null, null));
    }
}
