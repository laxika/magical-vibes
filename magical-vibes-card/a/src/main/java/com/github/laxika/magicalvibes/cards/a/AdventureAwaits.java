package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "177")
public class AdventureAwaits extends Card {

    public AdventureAwaits() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.CREATURE), new DrawCardEffect()));
    }
}
