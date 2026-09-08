package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "93")
public class SanguineSpy extends Card {

    public SanguineSpy() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeCreatureCost(false, false, false, true), new SurveilEffect(1)),
                "{1}, Sacrifice another creature: Surveil 1."
        ));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                        new MayPayLifeEffect(2, new DrawCardEffect(),
                                "Pay 2 life to draw a card?")));
    }
}
