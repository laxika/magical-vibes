package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "BRO", collectorNumber = "180")
public class FogOfWar extends Card {

    public FogOfWar() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentPowerAtLeastPredicate(4)));
    }
}
