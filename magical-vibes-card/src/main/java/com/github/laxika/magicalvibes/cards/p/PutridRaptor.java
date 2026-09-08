package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "71")
public class PutridRaptor extends Card {

    public PutridRaptor() {
        addMorph("{4}{B}{B}", new DiscardCardTypeCost(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), "Zombie"));
    }
}
