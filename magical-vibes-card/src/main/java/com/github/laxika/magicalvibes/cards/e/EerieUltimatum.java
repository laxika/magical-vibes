package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "IKO", collectorNumber = "184")
public class EerieUltimatum extends Card {

    public EerieUltimatum() {
        addEffect(EffectSlot.SPELL,
                ReturnCardsFromControllerGraveyardToBattlefieldEffect.anyNumberOfDistinctNames(
                        new CardIsPermanentPredicate()));
    }
}
