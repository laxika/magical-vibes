package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TDC", collectorNumber = "26")
@CardRegistration(set = "TDC", collectorNumber = "66")
public class BoneDevourer extends Card {

    public BoneDevourer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));

        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DrawCardEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                new LoseLifeEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        LoseLifeRecipient.CONTROLLER)));
    }
}
