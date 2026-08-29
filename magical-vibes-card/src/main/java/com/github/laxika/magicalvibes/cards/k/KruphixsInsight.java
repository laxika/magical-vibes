package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JOU", collectorNumber = "129")
public class KruphixsInsight extends Card {

    public KruphixsInsight() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealUpToToHandRestToGraveyard(
                6, new CardTypePredicate(CardType.ENCHANTMENT), new Fixed(3)));
    }
}
