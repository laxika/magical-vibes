package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "43")
public class TazeemRaptor extends Card {

    public TazeemRaptor() {
        // When this creature enters, you may return a land you control to its owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"),
                "You may return a land you control to its owner's hand."
        ));
    }
}
