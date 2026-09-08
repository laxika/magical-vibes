package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "146")
public class SwiftWarden extends Card {

    public SwiftWarden() {
        PermanentAllOfPredicate merfolkYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                new PermanentControlledBySourceControllerPredicate()
        ));

        target(new PermanentPredicateTargetFilter(merfolkYouControl,
                "Target must be a Merfolk you control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
