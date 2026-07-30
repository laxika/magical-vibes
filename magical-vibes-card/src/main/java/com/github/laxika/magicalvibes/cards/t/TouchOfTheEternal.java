package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetControllerLifeToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "M13", collectorNumber = "37")
public class TouchOfTheEternal extends Card {

    public TouchOfTheEternal() {
        // At the beginning of your upkeep, count the number of permanents you control.
        // Your life total becomes that number.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SetControllerLifeToAmountEffect(
                new PermanentCount(new PermanentTruePredicate(), CountScope.CONTROLLER)));
    }
}
