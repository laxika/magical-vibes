package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardThenSearchBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "133")
public class BorderlandExplorer extends Card {

    public BorderlandExplorer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerMayDiscardThenSearchBasicLandToHandEffect());
    }
}
