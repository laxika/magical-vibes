package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "173")
public class Antagonism extends Card {

    public Antagonism() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect(2));
    }
}
