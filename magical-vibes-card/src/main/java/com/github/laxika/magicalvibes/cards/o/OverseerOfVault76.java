package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "19")
@CardRegistration(set = "PIP", collectorNumber = "368")
@CardRegistration(set = "PIP", collectorNumber = "547")
@CardRegistration(set = "PIP", collectorNumber = "896")
public class OverseerOfVault76 extends Card {

    public OverseerOfVault76() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMaxPowerConditionalEffect(3,
                        new PutCountersOnSelfEffect(CounterType.QUEST)));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new RemoveCounterFromControlledPermanentCost(
                                List.of(CounterType.QUEST), 3, new PermanentTruePredicate(), false),
                        List.of(),
                        true,
                        List.of(
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsCreaturePredicate()),
                                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES)
                        )));
    }
}
