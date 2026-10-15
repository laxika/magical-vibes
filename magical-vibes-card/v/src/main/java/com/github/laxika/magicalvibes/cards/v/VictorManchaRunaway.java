package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;

@CardRegistration(set = "MSC", collectorNumber = "574")
public class VictorManchaRunaway extends Card {

    public VictorManchaRunaway() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(null, true, true));
    }
}
