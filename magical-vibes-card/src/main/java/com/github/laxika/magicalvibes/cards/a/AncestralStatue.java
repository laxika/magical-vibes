package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "234")
public class AncestralStatue extends Card {

    public AncestralStatue() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent you control"
                )),
                "Choose a nonland permanent you control to return to its owner's hand."
        ));
    }
}
