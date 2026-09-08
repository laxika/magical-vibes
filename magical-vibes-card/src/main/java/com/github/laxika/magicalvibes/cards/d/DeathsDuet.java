package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EXO", collectorNumber = "60")
@CardRegistration(set = "TPR", collectorNumber = "101")
public class DeathsDuet extends Card {

    public DeathsDuet() {
        addEffect(EffectSlot.SPELL, ReturnTargetCardsFromGraveyardToHandEffect.exactly(
                new CardTypePredicate(CardType.CREATURE), 2));
    }
}
