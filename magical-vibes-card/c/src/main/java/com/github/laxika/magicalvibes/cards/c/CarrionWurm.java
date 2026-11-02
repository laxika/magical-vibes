package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "55")
public class CarrionWurm extends Card {

    public CarrionWurm() {
        addTrigger(Triggers.attacks(), new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(3));
        addTrigger(Triggers.blocks(), new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(3));
    }
}
