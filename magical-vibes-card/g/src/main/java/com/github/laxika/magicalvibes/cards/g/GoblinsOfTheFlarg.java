package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "51")
@CardRegistration(set = "DRK", collectorNumber = "70")
@CardRegistration(set = "ME1", collectorNumber = "98")
public class GoblinsOfTheFlarg extends Card {

    public GoblinsOfTheFlarg() {
        // Mountainwalk is loaded from Scryfall metadata.
        // When you control a Dwarf, sacrifice this creature.
        addTrigger(Triggers.sourceState(new PermanentControllerControlsPermanentPredicate(
                        new PermanentHasSubtypePredicate(CardSubtype.DWARF))),
                new SacrificeSelfEffect());
    }
}
