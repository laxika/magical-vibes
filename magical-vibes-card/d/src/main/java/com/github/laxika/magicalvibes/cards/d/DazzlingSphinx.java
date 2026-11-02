package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "25")
@CardRegistration(set = "OTC", collectorNumber = "93")
public class DazzlingSphinx extends Card {

    public DazzlingSphinx() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect());
    }
}
