package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YWOE", collectorNumber = "19")
public class GrowOldTogether extends Card {

    public GrowOldTogether() {
        addEffect(EffectSlot.SPELL,
                new SeekFromTopOfLibraryEffect(10, 2, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new PerpetuallyBoostMatchingHandCardsEffect(
                new CardTypePredicate(CardType.CREATURE), 1, 1));
    }
}
