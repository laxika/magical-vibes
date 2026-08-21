package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "161")
@CardRegistration(set = "SUM", collectorNumber = "130")
public class Simulacrum extends Card {

    public Simulacrum() {
        // "You gain life equal to the damage dealt to you this turn."
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new DamageDealtToControllerThisTurn()));

        // "Simulacrum deals damage to target creature you control equal to the damage dealt to you this turn."
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new DamageDealtToControllerThisTurn()));
    }
}
