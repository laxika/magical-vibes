package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "54")
@CardRegistration(set = "PHUK", collectorNumber = "40")
@CardRegistration(set = "PHUK", collectorNumber = "41")
@CardRegistration(set = "PHUK", collectorNumber = "53")
public class CarrionRats extends Card {

    public CarrionRats() {
        addTrigger(Triggers.attacks(), new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect());
        addTrigger(Triggers.blocks(), new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect());
    }
}
