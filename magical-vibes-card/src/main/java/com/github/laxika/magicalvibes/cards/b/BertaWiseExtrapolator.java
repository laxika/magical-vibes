package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "175")
public class BertaWiseExtrapolator extends Card {

    public BertaWiseExtrapolator() {
        // Increment is driven automatically by the Scryfall-loaded INCREMENT keyword; no effect needed.

        // Whenever one or more +1/+1 counters are put on Berta, add one mana of any color.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT, new AwardAnyColorManaEffect());

        // {X}, {T}: Create a 0/0 green and blue Fractal creature token and put X +1/+1 counters on it.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new CreateXTokenWithXCountersEffect(
                        "Fractal", 0, 0,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL), CounterType.PLUS_ONE_PLUS_ONE)),
                "{X}, {T}: Create a 0/0 green and blue Fractal creature token and put X +1/+1 counters on it."
        ));
    }
}
