package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "27")
@CardRegistration(set = "WHO", collectorNumber = "345")
@CardRegistration(set = "WHO", collectorNumber = "632")
@CardRegistration(set = "WHO", collectorNumber = "936")
public class RomanaII extends Card {

    public RomanaII() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy()),
                "{1}, {T}: Create a tapped token that's a copy of target token that entered this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentEnteredBattlefieldThisTurnPredicate())),
                        "Target must be a token that entered this turn")
        ));
    }
}
