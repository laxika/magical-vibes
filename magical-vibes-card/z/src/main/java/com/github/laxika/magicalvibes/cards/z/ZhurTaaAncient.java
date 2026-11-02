package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "119")
public class ZhurTaaAncient extends Card {

    public ZhurTaaAncient() {
        // Symmetric: any player who taps a land for mana adds one extra mana of a type it produced.
        addTrigger(Triggers.playerTapsLandForMana(), new AddOneOfEachManaTypeProducedByLandEffect(false));
    }
}
