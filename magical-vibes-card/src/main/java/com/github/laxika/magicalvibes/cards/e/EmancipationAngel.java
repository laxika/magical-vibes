package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "19")
public class EmancipationAngel extends Card {

    public EmancipationAngel() {
        // When this creature enters, return a permanent you control to its owner's hand.
        // Mandatory, non-targeted resolution-time choice; this creature itself is a legal choice.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you control"
                )),
                "Choose a permanent you control to return to its owner's hand."
        ));
    }
}
