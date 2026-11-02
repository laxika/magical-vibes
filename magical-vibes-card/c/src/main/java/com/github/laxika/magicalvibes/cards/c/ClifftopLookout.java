package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToBottomEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "168")
@CardRegistration(set = "FDC", collectorNumber = "195")
public class ClifftopLookout extends Card {

    public ClifftopLookout() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealUntilLandToBattlefieldRestToBottomEffect(true, true));
    }
}
