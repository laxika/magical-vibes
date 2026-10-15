package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTwoCreaturesThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "216")
public class DyadrineSynthesisAmalgam extends Card {

    public DyadrineSynthesisAmalgam() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MayEffect(
                        new RemoveCounterFromTwoCreaturesThenEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                SequenceEffect.of(new DrawCardEffect(1), robotToken())),
                        "Remove a +1/+1 counter from each of two creatures you control?"));
    }

    private static CreateTokenEffect robotToken() {
        return new CreateTokenEffect("Robot", 2, 2, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT));
    }
}
