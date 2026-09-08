package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "33a")
@CardRegistration(set = "FEM", collectorNumber = "33b")
@CardRegistration(set = "FEM", collectorNumber = "33c")
@CardRegistration(set = "FEM", collectorNumber = "33d")
public class ArmorThrull extends Card {

    public ArmorThrull() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_TWO, 1)
                ),
                "{T}, Sacrifice this creature: Put a +1/+2 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
