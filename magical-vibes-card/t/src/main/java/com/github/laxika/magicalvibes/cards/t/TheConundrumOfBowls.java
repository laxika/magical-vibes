package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsControllerHandSizePredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueGreaterThanControllerHandSizePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "3")
public class TheConundrumOfBowls extends Card {

    public TheConundrumOfBowls() {
        addTrigger(Triggers.chapter(1), new SeekLibraryEffect(
                new Fixed(1),
                null,
                LibrarySearchDestination.HAND,
                new ManaValueBound(new CardsInHand(CountScope.CONTROLLER), false, -1)));
        addTrigger(Triggers.chapter(2),
                new SeekLibraryEffect(1, new CardManaValueGreaterThanControllerHandSizePredicate(),
                        LibrarySearchDestination.HAND));
        addTrigger(Triggers.chapter(3),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardManaValueEqualsControllerHandSizePredicate()));
    }
}
