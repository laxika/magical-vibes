package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "55")
public class CarrionWurm extends Card {

    public CarrionWurm() {
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE,
                new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(3));
    }
}
