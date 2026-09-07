package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "15")
public class DeliveryMoogle extends Card {

    public DeliveryMoogle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardMaxManaValuePredicate(2)))));
    }
}
