package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "190")
public class FractalTender extends Card {

    public FractalTender() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new PutCounterOnSourceThisTurn(),
                SequenceEffect.of(
                        new CreateTokenEffect(
                                "Fractal",
                                0,
                                0,
                                CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.BLUE),
                                List.of(CardSubtype.FRACTAL)
                        ),
                        new PutCountersOnCreatedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))
                )
        ));
    }
}
