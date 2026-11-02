package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "33")
@CardRegistration(set = "HOC", collectorNumber = "73")
@CardRegistration(set = "LTC", collectorNumber = "57")
@CardRegistration(set = "LTC", collectorNumber = "140")
@CardRegistration(set = "LTC", collectorNumber = "464")
public class GrMaSarumansFootman extends Card {

    public GrMaSarumansFootman() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect());
    }
}
