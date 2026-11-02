package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "65")
public class SteelgazeGriffin extends Card {

    public SteelgazeGriffin() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new BoostSelfEffect(2, 0));
    }
}
