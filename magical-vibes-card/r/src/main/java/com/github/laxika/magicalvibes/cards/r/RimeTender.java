package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "176")
public class RimeTender extends Card {

    public RimeTender() {
        PermanentPredicate anotherSnowPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, anotherSnowPermanent)),
                "{T}: Untap another target snow permanent.",
                new PermanentPredicateTargetFilter(anotherSnowPermanent, "Target must be another snow permanent")
        ));
    }
}
