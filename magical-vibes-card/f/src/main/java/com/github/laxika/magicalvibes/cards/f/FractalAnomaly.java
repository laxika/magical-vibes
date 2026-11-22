package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "50")
public class FractalAnomaly extends Card {

    public FractalAnomaly() {
        // Create a 0/0 green and blue Fractal creature token and put X +1/+1 counters on it,
        // where X is the number of cards you've drawn this turn.
        addSpellEffect(new CreateXTokenWithXCountersEffect("Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL),
                CounterType.PLUS_ONE_PLUS_ONE, new CardsDrawnThisTurn()));
    }
}
