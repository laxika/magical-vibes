package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AnimusOfNightsReach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "109")
public class TheLongReachOfNight extends Card {

    public TheLongReachOfNight() {
        setBackFaceCard(new AnimusOfNightsReach());

        // I, II — Each opponent sacrifices a creature of their choice unless they discard a card: every opponent
        // chooses first, then the chosen cards are discarded together and the others sacrifice (CR 101.4).
        addTrigger(Triggers.chapter(1), sacrificeUnlessDiscard());
        addTrigger(Triggers.chapter(2), sacrificeUnlessDiscard());
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    private static UnlessPaysEffect sacrificeUnlessDiscard() {
        return UnlessPaysEffect.eachUnless(Subjects.eachOpponent(), new DiscardCardTypeCost(null, null),
                opponents -> new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), opponents))
                .payingTogether();
    }

    @Override
    public String getBackFaceClassName() {
        return "AnimusOfNightsReach";
    }
}
