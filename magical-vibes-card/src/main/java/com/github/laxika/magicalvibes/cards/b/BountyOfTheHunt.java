package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "85")
@CardRegistration(set = "DKM", collectorNumber = "23")
public class BountyOfTheHunt extends Card {

    public BountyOfTheHunt() {
        // You may exile a green card from your hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.GREEN), "green"))));

        // Distribute three +1/+1 counters among one, two, or three target creatures. For each +1/+1
        // counter you put on a creature this way, remove a +1/+1 counter from that creature at the
        // beginning of the next cleanup step.
        addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenUntilNextCleanup(
                CounterType.PLUS_ONE_PLUS_ONE, 3));
    }
}
