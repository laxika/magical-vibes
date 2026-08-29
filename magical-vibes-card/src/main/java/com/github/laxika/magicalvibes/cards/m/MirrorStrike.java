package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "17")
public class MirrorStrike extends Card {

    public MirrorStrike() {
        target(TargetFilters.unblockedAttackingCreature())
                .addEffect(EffectSlot.SPELL, new RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect());
    }
}
