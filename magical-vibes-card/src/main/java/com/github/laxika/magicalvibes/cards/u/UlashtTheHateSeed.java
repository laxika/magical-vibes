package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "136")
public class UlashtTheHateSeed extends Card {

    private static final String ACTIVATION_COST = "{1}";

    public UlashtTheHateSeed() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.RED)))),
                        CountScope.CONTROLLER, true)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)))),
                        CountScope.CONTROLLER, true)));

        addActivatedAbility(new ActivatedAbility(
                false,
                ACTIVATION_COST,
                List.of(new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToTargetCreatureEffect(1)),
                "{1}, Remove a +1/+1 counter from Ulasht: Ulasht deals 1 damage to target creature.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                ACTIVATION_COST,
                List.of(new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{1}, Remove a +1/+1 counter from Ulasht: Create a 1/1 green Saproling creature token."
        ));
    }
}
