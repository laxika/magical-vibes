package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "214")
public class TrigonOfInfestation extends Card {

    public TrigonOfInfestation() {
        // Trigon of Infestation enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {G}{G}, {T}: Put a charge counter on Trigon of Infestation.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}",
                List.of(new PutChargeCounterOnSelfEffect()),
                "{G}{G}, {T}: Put a charge counter on Trigon of Infestation."
        ));

        // {2}, {T}, Remove a charge counter from Trigon of Infestation: Create a 1/1 green Phyrexian Insect creature token with infect.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new CreateTokenEffect("Phyrexian Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.INSECT),
                                Set.of(Keyword.INFECT), Set.of())
                ),
                "{2}, {T}, Remove a charge counter from Trigon of Infestation: Create a 1/1 green Phyrexian Insect creature token with infect."
        ));
    }
}
