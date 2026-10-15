package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "163")
public class CombineGuildmage extends Card {

    public CombineGuildmage() {
        // {1}{G}, {T}: This turn, each creature you control enters with an additional +1/+1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect(1)),
                "{1}{G}, {T}: This turn, each creature you control enters with an additional +1/+1 counter on it."
        ));

        // {1}{U}, {T}: Move a +1/+1 counter from target creature you control onto another target creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(MoveCounterFromTargetCreatureToTargetCreatureEffect.single(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{U}, {T}: Move a +1/+1 counter from target creature you control onto another target creature you control.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl()),
                2,
                2
        ));
    }
}
