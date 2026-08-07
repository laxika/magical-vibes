package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "175")
public class KikiJikiMirrorBreaker extends Card {

    public KikiJikiMirrorBreaker() {
        // {T}: Create a token that's a copy of target nonlegendary creature you control, except it has haste.
        // Sacrifice it at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenCopyOfTargetPermanentEffect(true, false, true)),
                "{T}: Create a token that's a copy of target nonlegendary creature you control, except it has haste. Sacrifice it at the beginning of the next end step.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
                        )),
                        "Target must be a nonlegendary creature you control.")));
    }
}
