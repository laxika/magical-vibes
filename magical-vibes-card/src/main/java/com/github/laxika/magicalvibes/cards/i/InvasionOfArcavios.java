package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "61")
public class InvasionOfArcavios extends Card {

    public InvasionOfArcavios() {
        setBackFaceCard(new InvocationOfTheFounders());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "InvocationOfTheFounders";
    }
}
