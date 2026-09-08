package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "103")
@CardRegistration(set = "DRK", collectorNumber = "104")
public class LivingArmor extends Card {

    public LivingArmor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ZERO_PLUS_ONE, new TargetManaValue())
                ),
                "{T}, Sacrifice this artifact: Put X +0/+1 counters on target creature, where X is that creature's mana value.",
                TargetFilters.creature()
        ));
    }
}
