package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "7")
@CardRegistration(set = "ME2", collectorNumber = "2")
public class AngelOfFury extends Card {

    public AngelOfFury() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, you may shuffle it into its owner's library."
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                "Shuffle Angel of Fury into its owner's library?"));
    }
}
