package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "123")
@CardRegistration(set = "MKM", collectorNumber = "404")
public class ExpeditedInheritance extends Card {

    public ExpeditedInheritance() {
        addTrigger(Triggers.creatureDealtDamage(), new MayEffect(
                ExileTopCardsMayPlayUntilNextTurnEffect.forTriggeringPermanentController(new EventValue()),
                "Exile that many cards from the top of your library?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
