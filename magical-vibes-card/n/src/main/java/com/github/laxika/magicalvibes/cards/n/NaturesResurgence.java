package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WTH", collectorNumber = "137")
@CardRegistration(set = "7ED", collectorNumber = "259")
@CardRegistration(set = "6ED", collectorNumber = "242")
public class NaturesResurgence extends Card {

    public NaturesResurgence() {
        // Each player draws a card for each creature card in their graveyard: the amount is evaluated
        // for each drawing player, so CONTROLLER scope counts each player's own graveyard.
        addSpellEffect(DrawCardEffect.eachEvaluatedFor(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER), Subjects.eachPlayer()));
    }
}
