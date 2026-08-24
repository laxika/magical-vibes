package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "45")
@CardRegistration(set = "DKM", collectorNumber = "2")
public class Contagion extends Card {

    public Contagion() {
        // You may pay 1 life and exile a black card from your hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new LifeCastingCost(1),
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLACK), "black"))));

        // Distribute two -2/-1 counters among one or two target creatures.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ), 1, 2).addEffect(EffectSlot.SPELL,
                DistributeCountersAmongTargetsEffect.evenlyAmongTargets(CounterType.MINUS_TWO_MINUS_ONE, 2));
    }
}
