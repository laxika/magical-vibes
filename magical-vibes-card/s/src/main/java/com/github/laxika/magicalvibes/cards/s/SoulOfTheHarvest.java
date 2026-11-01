package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "195")
@CardRegistration(set = "W16", collectorNumber = "16")
@CardRegistration(set = "E02", collectorNumber = "36")
@CardRegistration(set = "C14", collectorNumber = "215")
@CardRegistration(set = "CMA", collectorNumber = "148")
public class SoulOfTheHarvest extends Card {

    public SoulOfTheHarvest() {
        // The slot skips tokens and the entering permanent itself, covering "another nontoken".
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
