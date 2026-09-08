package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;

@CardRegistration(set = "MAT", collectorNumber = "18")
public class PlarggAndNassari extends Card {

    public PlarggAndNassari() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(2, true));
    }
}
