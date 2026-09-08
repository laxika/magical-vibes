package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "ROE", collectorNumber = "94")
public class ArrogantBloodlord extends Card {

    public ArrogantBloodlord() {
        PermanentPowerAtMostPredicate lowPower = new PermanentPowerAtMostPredicate(1);
        DestroySelfAtEndOfCombatEffect destroySelf = new DestroySelfAtEndOfCombatEffect();
        addEffect(EffectSlot.ON_BLOCK, new TriggeringPermanentConditionalEffect(lowPower, destroySelf));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(lowPower, destroySelf), TriggerMode.PER_BLOCKER);
    }
}
