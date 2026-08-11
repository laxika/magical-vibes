package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ZEN", collectorNumber = "82")
public class BloodchiefAscension extends Card {

    public BloodchiefAscension() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new OpponentLostLifeThisTurn(2),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Bloodchief Ascension?")));
        addEffect(EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.QUEST),
                        new MayEffect(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER, true),
                                "Have that player lose 2 life?")));
    }
}
