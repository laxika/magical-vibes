package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCountersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "140")
public class AmbitiousAugmenter extends Card {

    public AmbitiousAugmenter() {
        // Increment is driven automatically by the Scryfall-loaded INCREMENT keyword; no effect needed.

        // When this creature dies, if it had one or more counters on it, create a 0/0 green and blue
        // Fractal creature token, then put this creature's counters on that token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenWithDyingSourceCountersEffect(
                new CreateTokenEffect(
                        "Fractal", 0, 0,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL))));
    }
}
