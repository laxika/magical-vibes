package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "83")
@CardRegistration(set = "C16", collectorNumber = "63")
@CardRegistration(set = "VOC", collectorNumber = "82")
@CardRegistration(set = "CM2", collectorNumber = "23")
public class CustodiSoulbinders extends Card {

    public CustodiSoulbinders() {
        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, otherCreatures));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        CreateTokenEffect.whiteSpirit(1)
                ),
                "{2}{W}, Remove a +1/+1 counter from this creature: Create a 1/1 white Spirit creature token with flying."
        ));
    }
}
