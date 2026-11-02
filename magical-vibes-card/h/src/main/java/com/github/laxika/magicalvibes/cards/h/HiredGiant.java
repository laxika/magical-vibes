package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "194")
public class HiredGiant extends Card {

    public HiredGiant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentMaySearchLibraryForLandToBattlefieldEffect());
    }
}
