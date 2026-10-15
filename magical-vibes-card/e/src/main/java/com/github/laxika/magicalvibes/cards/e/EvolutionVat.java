package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "161")
public class EvolutionVat extends Card {

    public EvolutionVat() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{2}{G}{U}",
                                        List.of(new DoublePlusOneCountersOnSourceEffect()),
                                        "{2}{G}{U}: Double the number of +1/+1 counters on this creature."),
                                GrantScope.TARGET,
                                null,
                                EffectDuration.UNTIL_END_OF_TURN)
                ),
                "{3}, {T}: Tap target creature and put a +1/+1 counter on it. Until end of turn, that creature "
                        + "gains \"{2}{G}{U}: Double the number of +1/+1 counters on this creature.\"",
                TargetFilters.creature()
        ));
    }
}
