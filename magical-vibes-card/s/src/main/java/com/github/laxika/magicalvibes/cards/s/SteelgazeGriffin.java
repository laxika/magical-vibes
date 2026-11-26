package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "65")
public class SteelgazeGriffin extends Card {

    public SteelgazeGriffin() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), BoostEffect.self(2, 0));
    }
}
