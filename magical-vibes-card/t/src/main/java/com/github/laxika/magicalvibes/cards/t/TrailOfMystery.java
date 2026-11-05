package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "KTK", collectorNumber = "154")
@CardRegistration(set = "DSC", collectorNumber = "203")
@CardRegistration(set = "MKC", collectorNumber = "192")
@CardRegistration(set = "C19", collectorNumber = "186")
public class TrailOfMystery extends Card {

    public TrailOfMystery() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsFaceDownPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        "Search your library for a basic land card?"));
        addTrigger(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TargetPermanentMatches(new PermanentIsCreaturePredicate())),
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 2));
    }
}
