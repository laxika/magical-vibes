package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PCY", collectorNumber = "78")
public class SoulStrings extends Card {

    public SoulStrings() {
        addEffect(EffectSlot.SPELL, ReturnTargetCardsFromGraveyardToHandEffect.exactlyUnlessAnyPlayerPaysX(
                new CardTypePredicate(CardType.CREATURE), 2));
    }
}
