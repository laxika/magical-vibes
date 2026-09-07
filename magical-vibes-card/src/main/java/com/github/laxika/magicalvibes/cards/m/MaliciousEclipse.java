package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingThisTurnEffect;

@CardRegistration(set = "LCI", collectorNumber = "111")
public class MaliciousEclipse extends Card {

    public MaliciousEclipse() {
        // All creatures get -2/-2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));

        // If a creature an opponent controls would die this turn, exile it instead.
        addEffect(EffectSlot.SPELL, new ExileCreaturesInsteadOfDyingThisTurnEffect(true));
    }
}
