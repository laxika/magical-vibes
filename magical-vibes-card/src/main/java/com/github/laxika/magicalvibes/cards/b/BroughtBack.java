package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "M20", collectorNumber = "9")
public class BroughtBack extends Card {

    public BroughtBack() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardIsPermanentPredicate(), 2, true, true));
    }
}
