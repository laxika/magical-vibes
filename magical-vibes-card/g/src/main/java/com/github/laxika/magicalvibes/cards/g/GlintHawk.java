package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "10")
public class GlintHawk extends Card {

    public GlintHawk() {
        // When Glint Hawk enters the battlefield, sacrifice it unless you return
        // an artifact you control to its owner's hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.ARTIFACT));
    }
}
