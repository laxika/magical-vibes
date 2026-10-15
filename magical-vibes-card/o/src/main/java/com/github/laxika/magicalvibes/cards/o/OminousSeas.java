package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "61")
public class OminousSeas extends Card {

    public OminousSeas() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new PutCountersOnSelfEffect(CounterType.FORESHADOW));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(8, CounterType.FORESHADOW),
                        new CreateTokenEffect("Kraken", 8, 8, CardColor.BLUE,
                                List.of(CardSubtype.KRAKEN), Set.of(), Set.of())
                ),
                "Remove eight foreshadow counters from Ominous Seas: Create an 8/8 blue Kraken creature token."
        ));

        addCycling("{2}");
    }
}
