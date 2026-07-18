package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "5ED", collectorNumber = "398")
@CardRegistration(set = "4ED", collectorNumber = "345")
public class Shapeshifter extends Card {

    public Shapeshifter() {
        // "As this creature enters, choose a number between 0 and 7."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNumberOnEnterEffect(0, 7));
        // "At the beginning of your upkeep, you may choose a number between 0 and 7."
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new ChooseNumberEffect(0, 7), "Choose a number between 0 and 7?"));
        // "Shapeshifter's power is equal to the last chosen number and its toughness is equal to
        // 7 minus that number." — characteristic-defining P/T (power = N, toughness = 7 − N).
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new ChosenNumberOnSource(),
                new Sum(new Fixed(7), new Scaled(new ChosenNumberOnSource(), -1))));
    }
}
