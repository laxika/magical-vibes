package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BRO", collectorNumber = "50")
public class ForgingTheAnchor extends Card {

    public ForgingTheAnchor() {
        addEffect(EffectSlot.SPELL,
                new LookAtTopCardsEffect(new Fixed(5), new Fixed(5),
                        new CardTypePredicate(CardType.ARTIFACT), LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false, LibrarySearchDestination.HAND, true));
    }
}
