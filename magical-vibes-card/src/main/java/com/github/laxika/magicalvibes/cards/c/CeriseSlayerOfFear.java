package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SeekHighestManaValueCardEffect;

@CardRegistration(set = "YWOE", collectorNumber = "1")
public class CeriseSlayerOfFear extends Card {

    public CeriseSlayerOfFear() {
        addEffect(EffectSlot.SECOND_MAIN_PHASE_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new SeekHighestManaValueCardEffect(new LifeGainedThisTurn(CountScope.CONTROLLER))));
    }
}
