package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectCombatDamageToTargetAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "18")
public class TurnTheTables extends Card {

    public TurnTheTables() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new RedirectCombatDamageToTargetAttackingCreatureEffect());
    }
}
