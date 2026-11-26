package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "3")
@CardRegistration(set = "MM2", collectorNumber = "175")
public class EthercasteKnight extends Card {

    public EthercasteKnight() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Triggers.creatureYouControlAttacks() fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target, so BoostEffect.target boosts "that creature";
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                BoostEffect.target(1, 1));
    }
}
