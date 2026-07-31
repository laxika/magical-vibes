package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "123")
public class MishrasGroundbreaker extends Card {

    public MishrasGroundbreaker() {
        // {T}, Sacrifice this artifact: Target land becomes a 3/3 artifact creature that's still a
        // land. The animation is indefinite (PERMANENT duration); the artifact type is added
        // persistently so it survives end-of-turn cleanup like the 3/3 body does.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AnimatePermanentsEffect(
                                3, 3,
                                List.of(),
                                Set.of(),
                                null, Set.of(),
                                GrantScope.TARGET, EffectDuration.PERMANENT
                        ),
                        new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT, true)
                ),
                "{T}, Sacrifice this artifact: Target land becomes a 3/3 artifact creature that's still a land.",
                TargetFilters.land()
        ));
    }
}
