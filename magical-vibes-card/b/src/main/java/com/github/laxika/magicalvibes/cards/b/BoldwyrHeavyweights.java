package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "85")
public class BoldwyrHeavyweights extends Card {

    public BoldwyrHeavyweights() {
        // When this creature enters, each opponent may search their library for a creature card and
        // put it onto the battlefield. Then each player who searched their library this way shuffles.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect());
    }
}
