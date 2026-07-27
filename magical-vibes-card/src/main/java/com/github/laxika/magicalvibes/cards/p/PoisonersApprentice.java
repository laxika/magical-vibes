package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "SOS", collectorNumber = "92")
public class PoisonersApprentice extends Card {

    public PoisonersApprentice() {
        // Infusion — When this creature enters, target creature an opponent controls gets -4/-4
        // until end of turn if you gained life this turn. The -4/-4 only applies at resolution
        // when the intervening life-gain condition is met.
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new BoostTargetCreatureEffect(-4, -4)));
    }
}
