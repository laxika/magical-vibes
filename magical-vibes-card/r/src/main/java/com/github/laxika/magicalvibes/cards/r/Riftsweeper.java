package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardFromExileIntoOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "136")
@CardRegistration(set = "MMA", collectorNumber = "159")
public class Riftsweeper extends Card {

    public Riftsweeper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ShuffleTargetCardFromExileIntoOwnersLibraryEffect());
    }
}
