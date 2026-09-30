package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessReturnedLandHadNonbasicLandTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

/** Wonderscape Sage. */
@CardRegistration(set = "M3C", collectorNumber = "49")
@CardRegistration(set = "M3C", collectorNumber = "101")
public class WonderscapeSage extends Card {

    public WonderscapeSage() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate(), true),
                        new DrawCardEffect(1),
                        new DiscardUnlessReturnedLandHadNonbasicLandTypeEffect()),
                "{T}, Return a land you control to its owner's hand: Draw a card. Then discard a card unless that land had a nonbasic land type."
        ));
    }
}
