package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "285")
public class Energizer extends Card {

    public Energizer() {
        // {2}, {T}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "{2}, {T}: Put a +1/+1 counter on Energizer."
        ));
    }
}
