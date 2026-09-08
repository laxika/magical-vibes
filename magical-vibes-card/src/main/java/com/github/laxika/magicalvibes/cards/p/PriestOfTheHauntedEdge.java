package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "104")
public class PriestOfTheHauntedEdge extends Card {

    public PriestOfTheHauntedEdge() {
        // {T}, Sacrifice this creature: Target creature gets -X/-X until end of turn, where X is
        // the number of snow lands you control. Activate only as a sorcery.
        var snowLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
        ));
        var minusSnowLands = new Scaled(
                new PermanentCount(snowLand, CountScope.CONTROLLER),
                -1);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(minusSnowLands, minusSnowLands)
                ),
                "{T}, Sacrifice this creature: Target creature gets -X/-X until end of turn, where X is "
                        + "the number of snow lands you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
