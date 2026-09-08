package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UseToughnessForStationEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "EOE", collectorNumber = "209")
public class TapestryWarden extends Card {

    public TapestryWarden() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(
                GrantScope.ALL_OWN_CREATURES, new PermanentToughnessGreaterThanPowerPredicate()));
        addEffect(EffectSlot.STATIC, new UseToughnessForStationEffect());
    }
}
