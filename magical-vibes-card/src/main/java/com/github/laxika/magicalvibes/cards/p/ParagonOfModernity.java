package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrPutCounterIfExactlyThreeColorsEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "242")
public class ParagonOfModernity extends Card {

    public ParagonOfModernity() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new BoostSelfOrPutCounterIfExactlyThreeColorsEffect()),
                "{3}: This creature gets +1/+1 until end of turn. If exactly three colors of mana were spent to activate this ability, put a +1/+1 counter on it instead."
        ));
    }
}
