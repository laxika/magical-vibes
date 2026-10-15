package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "127")
@CardRegistration(set = "TLE", collectorNumber = "200")
public class ZukoFirebendingMaster extends Card {

    public ZukoFirebendingMaster() {
        addEffect(EffectSlot.ON_ATTACK,
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, new ControllerExperienceCounters()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.duringCombat(null, List.of(new ExperienceCountersEffect(1))));
    }
}
