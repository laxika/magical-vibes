package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToDamagedPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "233")
@CardRegistration(set = "FIN", collectorNumber = "320")
@CardRegistration(set = "FIN", collectorNumber = "400")
@CardRegistration(set = "FIN", collectorNumber = "498")
@CardRegistration(set = "FIN", collectorNumber = "545")
public class LightningArmyOfOne extends Card {

    public LightningArmyOfOne() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DoubleDamageToDamagedPlayerUntilNextTurnEffect());
    }
}
