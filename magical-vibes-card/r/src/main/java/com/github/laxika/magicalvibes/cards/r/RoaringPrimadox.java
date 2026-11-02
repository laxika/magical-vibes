package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "188")
@CardRegistration(set = "M15", collectorNumber = "196")
public class RoaringPrimadox extends Card {

    public RoaringPrimadox() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )),
                "Choose a creature you control to return to its owner's hand."
        ));
    }
}
