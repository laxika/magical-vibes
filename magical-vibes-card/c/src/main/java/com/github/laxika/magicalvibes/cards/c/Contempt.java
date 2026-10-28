package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "27")
public class Contempt extends Card {

    public Contempt() {
        // The attack trigger is the Aura's own ability, so the Aura is its source and its controller is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ATTACK, new ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect());
    }
}
