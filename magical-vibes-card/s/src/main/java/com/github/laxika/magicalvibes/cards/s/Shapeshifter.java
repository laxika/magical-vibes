package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "398")
@CardRegistration(set = "4ED", collectorNumber = "345")
@CardRegistration(set = "ATQ", collectorNumber = "64")
@CardRegistration(set = "ME4", collectorNumber = "226")
public class Shapeshifter extends Card {

    public Shapeshifter() {
        // "As this creature enters, choose a number between 0 and 7."
        addStaticEffect(new ChooseNumberOnEnterEffect(0, 7));
        // "At the beginning of your upkeep, you may choose a number between 0 and 7."
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new ChooseNumberEffect(0, 7), "Choose a number between 0 and 7?"));
        // "Shapeshifter's power is equal to the last chosen number and its toughness is equal to
        // 7 minus that number." — characteristic-defining P/T (power = N, toughness = 7 − N).
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new ChosenNumberOnSource(),
                new Sum(new Fixed(7), new Scaled(new ChosenNumberOnSource(), -1))));
    }
}
