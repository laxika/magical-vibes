package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "127")
@CardRegistration(set = "TLE", collectorNumber = "200")
public class ZukoFirebendingMaster extends Card {

    public ZukoFirebendingMaster() {
        addTrigger(Triggers.attacks(),
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, new ControllerExperienceCounters()));
        addTrigger(Triggers.youCastSpell().duringCombat(), new ExperienceCountersEffect(1));
    }
}
