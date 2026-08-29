package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "187")
public class FractalSummoning extends Card {

    public FractalSummoning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL)));
        addEffect(EffectSlot.SPELL, new PutCountersOnCreatedPermanentsEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
