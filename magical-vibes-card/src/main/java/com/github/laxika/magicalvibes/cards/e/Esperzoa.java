package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "25")
public class Esperzoa extends Card {

    public Esperzoa() {
        // At the beginning of your upkeep, return an artifact you control to its owner's hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact you control"
                )),
                "Choose an artifact you control to return to its owner's hand."
        ));
    }
}
