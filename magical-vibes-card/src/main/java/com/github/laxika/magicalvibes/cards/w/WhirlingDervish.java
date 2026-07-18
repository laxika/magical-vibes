package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "341")
@CardRegistration(set = "4ED", collectorNumber = "288")
public class WhirlingDervish extends Card {

    public WhirlingDervish() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));

        // At the beginning of each end step, if this creature dealt damage to an opponent this turn,
        // put a +1/+1 counter on it.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new SelfDealtDamageToOpponentThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
