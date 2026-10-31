package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesHaveRiotEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesWithCountersEffect;

@CardRegistration(set = "SLD", collectorNumber = "2008")
@CardRegistration(set = "MOC", collectorNumber = "35")
@CardRegistration(set = "MOC", collectorNumber = "122")
public class UncivilUnrest extends Card {

    public UncivilUnrest() {
        addStaticEffect(new ControlledCreaturesHaveRiotEffect());
        addStaticEffect(new DoubleDamageFromCreaturesWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
