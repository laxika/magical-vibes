package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesTargetPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsDuringCombatEffect;

@CardRegistration(set = "C19", collectorNumber = "46")
public class MarisiBreakerOfTheCoil extends Card {

    public MarisiBreakerOfTheCoil() {
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsDuringCombatEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null,
                        new GoadCreaturesTargetPlayerControlsUntilNextTurnEffect()));
    }
}
