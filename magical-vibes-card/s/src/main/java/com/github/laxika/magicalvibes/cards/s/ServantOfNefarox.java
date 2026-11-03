package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "108")
public class ServantOfNefarox extends Card {

    public ServantOfNefarox() {
        // Exalted: Triggers.creatureYouControlAttacks() fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target; AttacksAlone restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));
    }
}
