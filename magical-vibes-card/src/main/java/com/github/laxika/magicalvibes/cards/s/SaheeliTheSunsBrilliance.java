package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "239")
@CardRegistration(set = "LCI", collectorNumber = "308")
public class SaheeliTheSunsBrilliance extends Card {

    public SaheeliTheSunsBrilliance() {
        PermanentPredicate targetFilter = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsArtifactPredicate()
        ));
        targetFilter = new PermanentAllOfPredicate(List.of(
                targetFilter,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{R}",
                List.of(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(CardType.ARTIFACT), null, null, Map.of(), true, false, true, false
                )),
                "{U}{R}, {T}: Create a token that's a copy of another target creature or artifact you control, except it's an artifact in addition to its other types. It gains haste. Sacrifice it at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        targetFilter,
                        "Target must be another creature or artifact you control."
                )
        ));
    }
}
