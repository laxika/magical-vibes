package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "128")
@CardRegistration(set = "ELD", collectorNumber = "363")
public class IrencragPyromancer extends Card {

    public IrencragPyromancer() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new DealDamageToAnyTargetEffect(3));
    }
}
