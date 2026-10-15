package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "78")
public class KorozdaGorgon extends Card {

    public KorozdaGorgon() {
        // {2}, Remove a +1/+1 counter from a creature you control: Target creature gets -1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new BoostTargetCreatureEffect(-1, -1)
                ),
                "{2}, Remove a +1/+1 counter from a creature you control: Target creature gets -1/-1 until end of turn."
        ));
    }
}
