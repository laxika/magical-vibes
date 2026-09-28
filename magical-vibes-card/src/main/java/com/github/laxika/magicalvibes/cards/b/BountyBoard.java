package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "37")
@CardRegistration(set = "OTC", collectorNumber = "73")
public class BountyBoard extends Card {

    public BountyBoard() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.BOUNTY)),
                "{1}, {T}: Put a bounty counter on target creature. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect(2));
    }
}
