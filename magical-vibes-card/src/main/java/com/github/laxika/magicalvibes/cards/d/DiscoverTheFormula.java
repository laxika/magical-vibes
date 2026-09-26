package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YMID", collectorNumber = "15")
public class DiscoverTheFormula extends Card {

    public DiscoverTheFormula() {
        addEffect(EffectSlot.SPELL, new SeekCardsToHandEffect(
                new Fixed(3), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.SPELL, new PerpetuallyReduceCostForMatchingHandCardsEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 1));
    }
}
