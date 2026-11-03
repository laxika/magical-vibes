package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "94")
public class AlandraSkyDreamer extends Card {

    public AlandraSkyDreamer() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new CreateTokenEffect("Drake", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.DRAKE), Set.of(Keyword.FLYING), Set.of()));

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addTrigger(Triggers.youDrawNthCardEachTurn(5),
                new BoostAllOwnCreaturesEffect(cardsInHand, cardsInHand,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourcePermanentPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DRAKE)))));
    }
}
