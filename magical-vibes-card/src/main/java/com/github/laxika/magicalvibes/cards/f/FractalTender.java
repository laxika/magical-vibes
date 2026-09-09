package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "190")
public class FractalTender extends Card {

    public FractalTender() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new PutCounterOnSourceThisTurn(),
                new CreateTokenEffect(
                        "Fractal",
                        0,
                        0,
                        CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL),
                        3
                )
        ));
    }
}
