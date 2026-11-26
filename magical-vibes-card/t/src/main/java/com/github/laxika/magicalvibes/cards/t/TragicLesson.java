package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOU", collectorNumber = "51")
public class TragicLesson extends Card {

    public TragicLesson() {
        addSpellEffect(new DrawCardEffect(2));
        // Then discard a card unless you return a land you control to its owner's hand.
        addSpellEffect(UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(new PermanentIsLandPredicate(), "land you control"),
                new DiscardEffect(1, Subjects.you())));
    }
}
