package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect;

@CardRegistration(set = "IKO", collectorNumber = "221")
@CardRegistration(set = "MUL", collectorNumber = "42")
@CardRegistration(set = "MUL", collectorNumber = "107")
@CardRegistration(set = "MUL", collectorNumber = "172")
public class GyrudaDoomOfDepths extends Card {

    public GyrudaDoomOfDepths() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect(4));
    }
}
