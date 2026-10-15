package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;

@CardRegistration(set = "DGM", collectorNumber = "119")
public class ZhurTaaAncient extends Card {

    public ZhurTaaAncient() {
        // Symmetric: any player who taps a land for mana adds one extra mana of a type it produced.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddOneOfEachManaTypeProducedByLandEffect(false));
    }
}
