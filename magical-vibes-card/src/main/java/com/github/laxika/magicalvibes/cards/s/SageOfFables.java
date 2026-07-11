package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "47")
public class SageOfFables extends Card {

    public SageOfFables() {
        // Each other Wizard creature you control enters with an additional +1/+1 counter on it.
        addEffect(EffectSlot.STATIC, new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.WIZARD, 1));

        // {2}, Remove a +1/+1 counter from a creature you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{2}, Remove a +1/+1 counter from a creature you control: Draw a card."
        ));
    }
}
