package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "196")
@CardRegistration(set = "GK2", collectorNumber = "20")
public class SkymarkRoc extends Card {

    public SkymarkRoc() {
        // Flying is auto-loaded from Scryfall.
        // Whenever this creature attacks, you may return target creature defending player controls
        // with toughness 2 or less to its owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate(),
                        new PermanentToughnessAtMostPredicate(2))),
                "Target must be a creature defending player controls with toughness 2 or less"))
                .addTrigger(Triggers.attacks(),
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target creature to its owner's hand?"));
    }
}
