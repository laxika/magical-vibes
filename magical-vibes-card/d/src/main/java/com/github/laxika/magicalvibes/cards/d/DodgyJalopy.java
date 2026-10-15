package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "58")
@CardRegistration(set = "NCC", collectorNumber = "158")
public class DodgyJalopy extends Card {

    public DodgyJalopy() {
        GreatestManaValueAmongControlled greatestCreatureManaValue =
                new GreatestManaValueAmongControlled(new PermanentIsCreaturePredicate());
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(greatestCreatureManaValue, new Fixed(5)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, greatestCreatureManaValue)),
                "Scavenge {2}{G} ({2}{G}, Exile this card from your graveyard: Put a number of +1/+1 "
                        + "counters equal to this card's power on target creature. Scavenge only as a sorcery.)",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
