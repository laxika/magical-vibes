package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "IKO", collectorNumber = "65")
public class ReconnaissanceMission extends Card {

    public ReconnaissanceMission() {
        // Whenever a creature you control deals combat damage to a player, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));

        // Cycling {2}.
        addCycling("{2}");
    }
}
