package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileInstantOrSorcerySpellCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "219")
public class NivmagusElemental extends Card {

    public NivmagusElemental() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new ExileInstantOrSorcerySpellCost(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Exile an instant or sorcery spell you control: Put two +1/+1 counters on this creature."));
    }
}
