package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "116")
public class CityOfShadows extends Card {

    public CityOfShadows() {
        // {T}, Exile a creature you control: Put a storage counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExilePermanentCost(new PermanentIsCreaturePredicate(), "a creature"),
                        new PutCountersOnSelfEffect(CounterType.STORAGE)),
                "{T}, Exile a creature you control: Put a storage counter on this land."
        ));

        // {T}: Add {C} for each storage counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(
                        ManaColor.COLORLESS,
                        new CountersOnSource(CounterType.STORAGE))),
                "{T}: Add {C} for each storage counter on this land."
        ));
    }
}
