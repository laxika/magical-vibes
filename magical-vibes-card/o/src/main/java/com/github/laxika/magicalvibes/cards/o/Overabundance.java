package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "259")
public class Overabundance extends Card {

    public Overabundance() {
        addTrigger(Triggers.playerTapsLandForMana(), SequenceEffect.of(
                new AddOneOfEachManaTypeProducedByLandEffect(false),
                new DealDamageOnLandTapEffect(1)));
    }
}
