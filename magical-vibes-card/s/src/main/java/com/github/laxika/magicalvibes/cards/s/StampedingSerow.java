package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "150")
public class StampedingSerow extends Card {

    public StampedingSerow() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN))
                        )),
                        "Target must be a green creature you control"
                )),
                "Choose a green creature you control to return to its owner's hand."
        ));
    }
}
