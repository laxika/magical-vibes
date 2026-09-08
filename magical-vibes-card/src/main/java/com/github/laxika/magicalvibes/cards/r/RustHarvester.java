package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "159")
public class RustHarvester extends Card {

    public RustHarvester() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.ARTIFACT),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToAnyTargetEffect(new SourcePower())
                ),
                "{2}, {T}, Exile an artifact card from your graveyard: Put a +1/+1 counter on this creature, then it deals damage equal to its power to any target."
        ));
    }
}
