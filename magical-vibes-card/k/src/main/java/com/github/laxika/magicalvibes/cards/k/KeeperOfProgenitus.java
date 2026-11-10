package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "135")
public class KeeperOfProgenitus extends Card {

    public KeeperOfProgenitus() {
        // Whenever a player taps a Mountain, Forest, or Plains for mana,
        // that player adds one mana of any type that land produced.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasAnySubtypePredicate(
                        Set.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS))),
                new AddOneOfEachManaTypeProducedByLandEffect());
    }
}
