package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "74")
public class FontOfAgonies extends Card {

    public FontOfAgonies() {
        addEffect(EffectSlot.ON_CONTROLLER_PAYS_LIFE,
                new PutCountersOnSelfEffect(CounterType.BLOOD, new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new RemoveCounterFromSourceCost(4, CounterType.BLOOD),
                        new DestroyTargetPermanentEffect()
                ),
                "{1}{B}, Remove four blood counters from this enchantment: Destroy target creature.",
                TargetFilters.creature()
        ));
    }
}
