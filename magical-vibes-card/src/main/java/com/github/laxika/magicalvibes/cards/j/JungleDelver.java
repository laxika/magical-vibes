package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "195")
public class JungleDelver extends Card {

    public JungleDelver() {
        // {3}{G}: Put a +1/+1 counter on Jungle Delver.
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}",
                List.of(new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{3}{G}: Put a +1/+1 counter on Jungle Delver."));
    }
}
