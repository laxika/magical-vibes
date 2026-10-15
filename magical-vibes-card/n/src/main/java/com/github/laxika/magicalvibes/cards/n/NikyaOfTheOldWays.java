package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;

@CardRegistration(set = "RNA", collectorNumber = "193")
public class NikyaOfTheOldWays extends Card {

    public NikyaOfTheOldWays() {
        addEffect(EffectSlot.STATIC, new NoncreatureSpellsCantBeCastEffect(0, false, false));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddOneOfEachManaTypeProducedByLandEffect(true));
    }
}
