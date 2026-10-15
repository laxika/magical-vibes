package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "TMP", collectorNumber = "146")
public class MinionOfTheWastes extends Card {

    public MinionOfTheWastes() {
        // "As this creature enters, pay any amount of life."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PayAnyAmountOfLifeOnEnterEffect());
        // "Minion of the Wastes's power and toughness are each equal to the life paid as it
        // entered." — characteristic-defining P/T reading the stored payment.
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new ChosenNumberOnSource(), new ChosenNumberOnSource()));
    }
}
