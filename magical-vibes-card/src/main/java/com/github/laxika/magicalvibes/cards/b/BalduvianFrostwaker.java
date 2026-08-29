package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "28")
public class BalduvianFrostwaker extends Card {

    public BalduvianFrostwaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new AnimatePermanentsEffect(
                        2, 2,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.FLYING),
                        CardColor.BLUE,
                        Set.of(),
                        GrantScope.TARGET,
                        EffectDuration.PERMANENT)),
                "{U}, {T}: Target snow land becomes a 2/2 blue Elemental creature with flying. It's still a land.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                        )),
                        "Target must be a snow land")));
    }
}
