package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetCreatureStatEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "17")
public class DazzlingReflection extends Card {

    public DazzlingReflection() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GainLifeEqualToTargetCreatureStatEffect(new TargetPower()))
                .addEffect(EffectSlot.SPELL, new PreventNextDamageByTargetCreatureEffect(false));
    }
}
