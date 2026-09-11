package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "204")
public class TheAstonishingAntMan extends Card {

    public TheAstonishingAntMan() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect(new XValue(), "Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of())
                ),
                "{2}{G}, {T}, Remove any number of +1/+1 counters from The Astonishing Ant-Man: "
                        + "Create that many 1/1 green Insect creature tokens."
        ));
    }
}
