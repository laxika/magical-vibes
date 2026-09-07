package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "115")
public class PrecipitousDrop extends Card {

    public PrecipitousDrop() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new ControllerHasCompletedDungeon(),
                        new StaticBoostEffect(-3, -3, GrantScope.ENCHANTED_CREATURE)));
    }
}
