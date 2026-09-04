package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "190")
public class GoblinSkiPatrol extends Card {

    public GoblinSkiPatrol() {
        // {1}{R}: This creature gets +2/+0 and gains flying. Its controller sacrifices it at the
        // beginning of the next end step. Activate only once and only if you control a snow Mountain.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{R}",
                List.of(
                        new BoostSelfEffect(2, 0, EffectDuration.PERMANENT),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF, GrantDuration.INDEFINITE),
                        new SacrificeSelfAtEndStepEffect()
                ),
                "{1}{R}: This creature gets +2/+0 and gains flying. Its controller sacrifices it at "
                        + "the beginning of the next end step. Activate only once and only if you "
                        + "control a snow Mountain."
        )
                .withMaxActivationsPerGame(1)
                .withRequiredControlledPermanents(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                        )),
                        1,
                        "snow Mountains"));
    }
}
