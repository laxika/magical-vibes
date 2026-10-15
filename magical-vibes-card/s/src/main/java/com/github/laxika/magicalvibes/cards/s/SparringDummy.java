package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledLandToHandEffect;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "197")
public class SparringDummy extends Card {

    public SparringDummy() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillControllerAndMayReturnMilledLandToHandEffect(),
                        new ConditionalEffect(new EventValueAtLeast(1), new GainLifeEffect(2))),
                "{T}: Mill a card. You may put a land card milled this way into your hand. You gain 2 life if a Lesson card is milled this way."
        ));
    }
}
