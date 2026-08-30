package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "131")
public class IncubationDruid extends Card {

    public IncubationDruid() {
        SourceCounterThreshold hasPlusOneCounter =
                new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfTypeLandsCouldProduceEffect(
                        ManaColorLandScope.CONTROLLER,
                        new PermanentIsLandPredicate(),
                        new FixedIfCondition(hasPlusOneCounter, 3, 1))),
                "{T}: Add one mana of any type that a land you control could produce. If this creature has a +1/+1 counter on it, add three mana of that type instead."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(new AdaptEffect(3)),
                "{3}{G}{G}: Adapt 3."
        ));
    }
}
