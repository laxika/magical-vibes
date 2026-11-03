package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksNotController;
import com.github.laxika.magicalvibes.model.effect.AttackingPlayerChoosesCreatureToBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "62")
@CardRegistration(set = "LTC", collectorNumber = "144")
@CardRegistration(set = "LTC", collectorNumber = "469")
public class MirkwoodTrapper extends Card {

    public MirkwoodTrapper() {
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.creaturesAttackYou(),
                        new BoostTargetCreatureEffect(-2, 0));

        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new PlayerAttacksNotController()),
                new AttackingPlayerChoosesCreatureToBoostEffect(2, 0));
    }
}
