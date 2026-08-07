package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "185")
public class LlanowarEmpath extends Card {

    public LlanowarEmpath() {
        // When this creature enters, scry 2, then reveal the top card of your library.
        // If it's a creature card, put it into your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardMatchingToHandEffect(new CardTypePredicate(CardType.CREATURE)));
    }
}
