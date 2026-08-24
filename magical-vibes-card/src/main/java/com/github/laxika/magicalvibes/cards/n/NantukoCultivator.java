package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TOR", collectorNumber = "133")
public class NantukoCultivator extends Card {

    public NantukoCultivator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardsAndPutCountersOnSourceEffect(
                        new CardTypePredicate(CardType.LAND), 1, 1, "land cards"),
                "Discard any number of land cards to put that many +1/+1 counters on Nantuko Cultivator and draw that many cards?"
        ));
    }
}
