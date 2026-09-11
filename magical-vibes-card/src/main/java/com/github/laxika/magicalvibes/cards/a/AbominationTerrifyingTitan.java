package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "198")
public class AbominationTerrifyingTitan extends Card {

    public AbominationTerrifyingTitan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R/G}{R/G}",
                List.of(
                        new PutCountersOnSourceCardEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new SourceFightsTargetCreatureEffect()
                ),
                "Power-up — {5}{R/G}{R/G}: Put a +1/+1 counter on Abomination. He fights up to one target creature an opponent controls.",
                TargetFilters.creatureAnOpponentControls(),
                null,
                null,
                null,
                List.of(),
                0,
                1
        ).withPowerUp());
    }
}
