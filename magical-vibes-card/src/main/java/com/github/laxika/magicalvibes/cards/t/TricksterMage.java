package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NEM", collectorNumber = "49")
public class TricksterMage extends Card {

    public TricksterMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target permanent?")
                ),
                "{U}, {T}, Discard a card: You may tap or untap target artifact, creature, or land.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        "Target must be an artifact, creature, or land"
                )
        ));
    }
}
