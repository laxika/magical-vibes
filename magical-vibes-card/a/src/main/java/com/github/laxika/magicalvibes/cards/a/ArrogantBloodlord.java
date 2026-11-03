package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "94")
public class ArrogantBloodlord extends Card {

    public ArrogantBloodlord() {
        PermanentPowerAtMostPredicate lowPower = new PermanentPowerAtMostPredicate(1);
        DestroySelfAtEndOfCombatEffect destroySelf = new DestroySelfAtEndOfCombatEffect();
        addTrigger(Triggers.blocks().withFilter(lowPower), destroySelf);
        addTrigger(Triggers.becomesBlocked().withFilter(lowPower), TriggerOptions.EACH_BLOCKER, destroySelf);
    }
}
