package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "107")
public class GlitteringStockpile extends Card {

    public GlitteringStockpile() {
        // {T}: Add {R}. Put a stash counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new PutCountersOnSelfEffect(CounterType.STASH)),
                "{T}: Add {R}. Put a stash counter on this artifact."
        ));

        // {T}, Sacrifice this artifact: Add X mana of any one color, where X is the number of
        // stash counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect(new CountersOnSource(CounterType.STASH))),
                "{T}, Sacrifice this artifact: Add X mana of any one color, where X is the number of stash counters on this artifact."
        ));
    }
}
