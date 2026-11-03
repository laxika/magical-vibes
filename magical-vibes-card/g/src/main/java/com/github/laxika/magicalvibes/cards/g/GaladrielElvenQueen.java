package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.GaladrielElvenQueenEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "3")
@CardRegistration(set = "LTC", collectorNumber = "83")
@CardRegistration(set = "LTC", collectorNumber = "88")
@CardRegistration(set = "LTC", collectorNumber = "462")
public class GaladrielElvenQueen extends Card {

    public GaladrielElvenQueen() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.ELF))),
                new GaladrielElvenQueenEffect());
    }
}
