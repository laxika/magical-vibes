package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "240")
public class DeepwoodElder extends Card {

    public DeepwoodElder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{G}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantBasicLandTypeToTargetEffect(
                                EffectDuration.UNTIL_END_OF_TURN, CardSubtype.FOREST, true)
                ),
                "{X}{G}{G}, {T}, Discard a card: X target lands become Forests until end of turn.",
                TargetFilters.land(),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());
    }
}
