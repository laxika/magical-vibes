package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "123")
@CardRegistration(set = "MKM", collectorNumber = "404")
public class ExpeditedInheritance extends Card {

    public ExpeditedInheritance() {
        PlayerSubject itsController = Subjects.controllerOf(Subjects.triggering());
        addTrigger(Triggers.creatureDealtDamage(), new MayEffect(
                new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false)
                        .fromLibraryOf(itsController)
                        .playableBy(itsController)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN),
                "Exile that many cards from the top of your library?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
