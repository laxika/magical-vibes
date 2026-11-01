package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardOfChosenCreatureTypeIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "124")
public class ElvishSoultiller extends Card {

    public ElvishSoultiller() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ShuffleGraveyardOfChosenCreatureTypeIntoLibraryEffect());
    }
}
