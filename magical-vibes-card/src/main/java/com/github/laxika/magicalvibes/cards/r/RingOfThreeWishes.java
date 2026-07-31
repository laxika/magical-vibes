package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "216")
public class RingOfThreeWishes extends Card {

    public RingOfThreeWishes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.WISH, new Fixed(3)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.WISH),
                        new SearchLibraryEffect()
                ),
                "{5}, {T}, Remove a wish counter from this artifact: Search your library for a card, put that card into your hand, then shuffle."
        ));
    }
}
