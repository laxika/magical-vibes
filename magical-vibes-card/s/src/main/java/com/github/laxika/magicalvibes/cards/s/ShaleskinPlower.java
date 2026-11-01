package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "110")
public class ShaleskinPlower extends Card {

    public ShaleskinPlower() {
        addMorph("{4}{R}");
        target(TargetFilters.land())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
