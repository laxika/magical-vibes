package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect;

@CardRegistration(set = "PIP", collectorNumber = "113")
@CardRegistration(set = "PIP", collectorNumber = "423")
@CardRegistration(set = "PIP", collectorNumber = "641")
@CardRegistration(set = "PIP", collectorNumber = "951")
public class TheNiptonLottery extends Card {

    public TheNiptonLottery() {
        addEffect(EffectSlot.SPELL,
                new ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect());
    }
}
