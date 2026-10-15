package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "FRF", collectorNumber = "147")
public class WindsOfQalSisma extends Card {

    public WindsOfQalSisma() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                PreventDamageEffect.allCombat(),
                PreventDamageEffect.allCombatExcept(new PermanentControlledBySourceControllerPredicate())));
    }
}
