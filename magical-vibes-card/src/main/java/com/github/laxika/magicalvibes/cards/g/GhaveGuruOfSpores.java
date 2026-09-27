package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1137")
@CardRegistration(set = "2X2", collectorNumber = "216")
@CardRegistration(set = "CMD", collectorNumber = "200")
public class GhaveGuruOfSpores extends Card {

    public GhaveGuruOfSpores() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));

        // {1}, Remove a +1/+1 counter from a creature you control: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect(
                                "Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ),
                "{1}, Remove a +1/+1 counter from a creature you control: Create a 1/1 green Saproling creature token."
        ));

        // {1}, Sacrifice a creature: Put a +1/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{1}, Sacrifice a creature: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
