package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "102")
@CardRegistration(set = "M19", collectorNumber = "103")
@CardRegistration(set = "M21", collectorNumber = "105")
public class InfernalScarring extends Card {

    public InfernalScarring() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_DEATH,
                                new DrawCardEffect(1),
                                GrantScope.ENCHANTED_CREATURE));
    }
}
