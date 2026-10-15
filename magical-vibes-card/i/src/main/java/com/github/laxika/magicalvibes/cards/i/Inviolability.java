package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "23")
public class Inviolability extends Card {

    public Inviolability() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToAttachedCreatureEffect())
                .addEffect(EffectSlot.STATIC, new PreventAllNoncombatDamageToAttachedCreatureEffect());
    }
}
