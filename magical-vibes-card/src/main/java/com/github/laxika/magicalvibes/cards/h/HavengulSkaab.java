package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "60")
public class HavengulSkaab extends Card {

    public HavengulSkaab() {
        // Whenever this creature attacks, return another creature you control to its owner's hand.
        // Mandatory and non-targeting: the creature is chosen at resolution among the controller's
        // other creatures; "another" excludes the Skaab itself.
        addEffect(EffectSlot.ON_ATTACK, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "creature"
        ));
    }
}
