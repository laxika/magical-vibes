package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "112")
@CardRegistration(set = "PLS", collectorNumber = "37")
@CardRegistration(set = "PC2", collectorNumber = "26")
@CardRegistration(set = "PCA", collectorNumber = "26")
public class SunkenHope extends Card {

    public SunkenHope() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER,
                Set.of(new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )),
                "Choose a creature you control to return to its owner's hand."
        ));
    }
}
