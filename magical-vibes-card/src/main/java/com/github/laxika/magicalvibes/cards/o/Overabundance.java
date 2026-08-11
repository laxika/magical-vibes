package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;

@CardRegistration(set = "INV", collectorNumber = "259")
public class Overabundance extends Card {

    public Overabundance() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddOneOfEachManaTypeProducedByLandEffect(false));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new DealDamageOnLandTapEffect(1));
    }
}
