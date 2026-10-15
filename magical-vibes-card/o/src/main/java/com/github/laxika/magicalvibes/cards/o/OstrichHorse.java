package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "188")
public class OstrichHorse extends Card {

    public OstrichHorse() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
                        3, new CardTypePredicate(CardType.LAND)));
    }
}
