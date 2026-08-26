package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "57")
public class InnerDemonsGangsters extends Card {

    public InnerDemonsGangsters() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "Discard a card: This creature gets +1/+0 and gains menace until end of turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
