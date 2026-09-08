package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSameTotalPowerToughnessEffect;

@CardRegistration(set = "PLC", collectorNumber = "144")
public class WildPair extends Card {

    public WildPair() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new SearchLibraryForCreatureWithSameTotalPowerToughnessEffect());
    }
}
