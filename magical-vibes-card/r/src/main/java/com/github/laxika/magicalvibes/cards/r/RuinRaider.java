package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "118")
public class RuinRaider extends Card {

    public RuinRaider() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // reveal the top card of your library and put that card into your hand.
        // You lose life equal to the card's mana value.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
