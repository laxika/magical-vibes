package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "82")
@CardRegistration(set = "USG", collectorNumber = "81")
public class ImaginaryPet extends Card {

    public ImaginaryPet() {
        // At the beginning of your upkeep, if you have a card in hand, return this creature to its owner's hand.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new CardsInHandAtLeast(1)),
                ReturnToHandEffect.self());
    }
}
