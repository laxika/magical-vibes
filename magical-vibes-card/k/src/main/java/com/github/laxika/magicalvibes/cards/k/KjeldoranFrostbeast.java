package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "ICE", collectorNumber = "296")
@CardRegistration(set = "ME3", collectorNumber = "156")
public class KjeldoranFrostbeast extends Card {

    public KjeldoranFrostbeast() {
        addTrigger(Triggers.endOfCombat(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceAttackedOrBlockedThisCombat()),
                new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(false, true));
    }
}
