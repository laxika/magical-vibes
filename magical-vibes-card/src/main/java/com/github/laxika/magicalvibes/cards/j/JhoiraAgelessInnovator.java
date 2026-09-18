package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutArtifactFromHandWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "202")
public class JhoiraAgelessInnovator extends Card {

    public JhoiraAgelessInnovator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.INGENUITY, 2),
                        new PutArtifactFromHandWithManaValueAtMostSourceCountersEffect(CounterType.INGENUITY)
                ),
                "{T}: Put two ingenuity counters on Jhoira, then you may put an artifact card with mana value X "
                        + "or less from your hand onto the battlefield, where X is the number of ingenuity counters "
                        + "on Jhoira."
        ));
    }
}
