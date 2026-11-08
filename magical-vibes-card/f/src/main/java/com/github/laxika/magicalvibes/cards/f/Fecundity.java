package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "247")
@CardRegistration(set = "USG", collectorNumber = "251")
@CardRegistration(set = "BRB", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1714")
@CardRegistration(set = "UMA", collectorNumber = "165")
@CardRegistration(set = "C13", collectorNumber = "145")
public class Fecundity extends Card {

    public Fecundity() {
        // Whenever a creature dies, that creature's controller may draw a card.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new MayEffect(new DrawCardForTriggeringPermanentControllerEffect(), "Draw a card?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
