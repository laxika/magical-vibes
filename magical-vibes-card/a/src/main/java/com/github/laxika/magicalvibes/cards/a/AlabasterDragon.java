package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "1")
@CardRegistration(set = "WTH", collectorNumber = "2")
public class AlabasterDragon extends Card {

    public AlabasterDragon() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, shuffle it into its owner's library."
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
