package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "228")
public class ShapersOfNature extends Card {

    public ShapersOfNature() {
        // {3}{G}: Put a +1/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1)),
                "{3}{G}: Put a +1/+1 counter on target creature."
        ));

        // {2}{U}, Remove a +1/+1 counter from a creature you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{2}{U}, Remove a +1/+1 counter from a creature you control: Draw a card."
        ));
    }
}
