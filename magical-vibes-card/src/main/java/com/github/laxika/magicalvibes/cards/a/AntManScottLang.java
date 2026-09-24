package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "513")
public class AntManScottLang extends Card {

    public AntManScottLang() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{4}: Put a +1/+1 counter on Ant-Man."
        ));
    }
}
