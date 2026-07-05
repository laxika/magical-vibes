package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

@CardRegistration(set = "SOM", collectorNumber = "113")
public class BluntTheAssault extends Card {

    public BluntTheAssault() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());
    }
}
