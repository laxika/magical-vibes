package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "136")
public class FeveredConvulsions extends Card {

    public FeveredConvulsions() {
        // {2}{B}{B}: Put a -1/-1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                "{2}{B}{B}: Put a -1/-1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
