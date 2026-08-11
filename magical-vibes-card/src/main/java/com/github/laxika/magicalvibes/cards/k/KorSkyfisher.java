package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "23")
public class KorSkyfisher extends Card {

    public KorSkyfisher() {
        // When this creature enters, return a permanent you control to its owner's hand.
        // Non-targeting choice at resolution; may (and alone, must) return itself.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentTruePredicate(),
                "permanent"
        ));
    }
}
