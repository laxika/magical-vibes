package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffect;

@CardRegistration(set = "ODY", collectorNumber = "197")
public class ImpulsiveManeuvers extends Card {

    public ImpulsiveManeuvers() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffect());
    }
}
