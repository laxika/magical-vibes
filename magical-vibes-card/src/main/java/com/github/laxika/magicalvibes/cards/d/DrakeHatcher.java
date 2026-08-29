package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "35")
public class DrakeHatcher extends Card {

    public DrakeHatcher() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCountersOnSelfEffect(CounterType.INCUBATION, new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.INCUBATION),
                        new CreateTokenEffect("Drake", 2, 2, CardColor.BLUE,
                                List.of(CardSubtype.DRAKE), Set.of(Keyword.FLYING), Set.of())
                ),
                "Remove three incubation counters from this creature: Create a 2/2 blue Drake creature token with flying."
        ));
    }
}
