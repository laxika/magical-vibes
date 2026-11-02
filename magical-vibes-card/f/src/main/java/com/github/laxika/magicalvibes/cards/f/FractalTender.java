package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "190")
public class FractalTender extends Card {

    public FractalTender() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
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
