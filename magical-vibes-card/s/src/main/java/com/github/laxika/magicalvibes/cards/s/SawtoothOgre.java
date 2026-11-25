package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "114")
public class SawtoothOgre extends Card {

    public SawtoothOgre() {
        // Whenever this creature blocks or becomes blocked by a creature, this creature deals
        // 1 damage to that creature at end of combat.
        RegisterDelayedStepTriggerEffect damage = RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endOfCombat(),
                Subjects.triggering(), DealDamageEffect.to(Subjects.triggering(), 1));
        addTrigger(Triggers.blocks(), damage);
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, damage);
    }
}
