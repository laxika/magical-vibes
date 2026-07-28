package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "309")
public class ArcumsSleigh extends Card {

    public ArcumsSleigh() {
        // {2}, {T}: Target creature gains vigilance until end of turn.
        // Activate only during combat and only if defending player controls a snow land.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)),
                "{2}, {T}: Target creature gains vigilance until end of turn. "
                        + "Activate only during combat and only if defending player controls a snow land.",
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ).withActivationCondition(
                new DefendingPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW)))),
                "Activate only if defending player controls a snow land"
        ));
    }
}
