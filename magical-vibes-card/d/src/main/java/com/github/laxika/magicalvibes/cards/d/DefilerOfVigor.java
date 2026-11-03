package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeToReduceColoredCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "160")
public class DefilerOfVigor extends Card {

    private static CardAllOfPredicate greenPermanentSpell() {
        return new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.GREEN),
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))
        ));
    }

    public DefilerOfVigor() {
        CardAllOfPredicate greenPermanentFilter = greenPermanentSpell();
        addStaticEffect(new PayLifeToReduceColoredCastCostEffect(
                greenPermanentFilter, "{G}", 2, CostModificationScope.SELF));
        addTrigger(Triggers.youCastSpell().withFilter(greenPermanentFilter),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
