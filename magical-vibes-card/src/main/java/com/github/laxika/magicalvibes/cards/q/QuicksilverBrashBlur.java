package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "148")
public class QuicksilverBrashBlur extends Card {

    public QuicksilverBrashBlur() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Quicksilver, Brash Blur on the battlefield?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.DOUBLE_STRIKE)
                ),
                "Power-up — {4}{R}: Put a +1/+1 counter and a double strike counter on Quicksilver."
                        + " (Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn.)"
        ).withPowerUp());
    }
}
