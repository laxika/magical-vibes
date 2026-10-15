package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "INV", collectorNumber = "259")
public class Overabundance extends Card {

    public Overabundance() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, SequenceEffect.of(
                new AddOneOfEachManaTypeProducedByLandEffect(false),
                new DealDamageOnLandTapEffect(1)));
    }
}
