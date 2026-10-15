package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "310")
public class ArcumsWeathervane extends Card {

    public ArcumsWeathervane() {
        // {2}, {T}: Target snow land is no longer snow.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new SetTargetPermanentSupertypeEffect(CardSupertype.SNOW, false)),
                "{2}, {T}: Target snow land is no longer snow.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                        "Target must be a snow land")
        ));

        // {2}, {T}: Target nonsnow basic land becomes snow.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new SetTargetPermanentSupertypeEffect(CardSupertype.SNOW, true)),
                "{2}, {T}: Target nonsnow basic land becomes snow.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.SNOW)))),
                        "Target must be a nonsnow basic land")
        ));
    }
}
