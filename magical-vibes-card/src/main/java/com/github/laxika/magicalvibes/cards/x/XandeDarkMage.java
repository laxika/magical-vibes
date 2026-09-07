package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "516")
@CardRegistration(set = "FIN", collectorNumber = "561")
public class XandeDarkMage extends Card {

    public XandeDarkMage() {
        CardAllOfPredicate noncreatureNonland = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        CardsInGraveyard noncreatureNonlandCardsInGraveyard =
                new CardsInGraveyard(noncreatureNonland, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new BoostSelfEffect(noncreatureNonlandCardsInGraveyard, noncreatureNonlandCardsInGraveyard));
    }
}
