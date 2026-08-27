package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "251")
@CardRegistration(set = "MKM", collectorNumber = "422")
public class Cryptex extends Card {

    public Cryptex() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new CollectEvidenceCost(3),
                        new AwardAnyColorManaEffect(),
                        new PutCountersOnSelfEffect(CounterType.UNLOCK)
                ),
                "{T}, Collect evidence 3: Add one mana of any color. Put an unlock counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new SurveilEffect(3), new DrawCardEffect(3)),
                "Sacrifice this artifact: Surveil 3, then draw three cards. Activate only if this artifact has five or more unlock counters on it."
        ).withRequiredSourceCounters(CounterType.UNLOCK, 5));
    }
}
