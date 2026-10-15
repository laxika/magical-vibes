package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;

@CardRegistration(set = "JOU", collectorNumber = "121")
public class DictateOfKarametra extends Card {

    public DictateOfKarametra() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddOneOfEachManaTypeProducedByLandEffect(false));
    }
}
