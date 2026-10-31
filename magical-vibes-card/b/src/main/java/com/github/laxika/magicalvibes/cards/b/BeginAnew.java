package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "YMID", collectorNumber = "57")
public class BeginAnew extends Card {

    public BeginAnew() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(new PerpetuallyBoostMatchingHandCardsEffect(
                new CardTypePredicate(CardType.CREATURE), 1, 1));
    }
}
