package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "209")
public class TownGreeter extends Card {

    public TownGreeter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        4,
                        new CardTypePredicate(CardType.LAND),
                        new CardSubtypePredicate(CardSubtype.TOWN),
                        2));
    }
}
