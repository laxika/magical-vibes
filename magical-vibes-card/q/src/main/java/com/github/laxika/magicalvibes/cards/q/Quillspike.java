package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "126")
public class Quillspike extends Card {

    public Quillspike() {
        // {B/G}, Remove a -1/-1 counter from a creature you control: This creature gets +3/+3 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/G}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.MINUS_ONE_MINUS_ONE),
                        new BoostSelfEffect(3, 3)
                ),
                "{B/G}, Remove a -1/-1 counter from a creature you control: This creature gets +3/+3 until end of turn."
        ));
    }
}
