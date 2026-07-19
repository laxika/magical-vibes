package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CON", collectorNumber = "27")
public class FaerieMechanist extends Card {

    public FaerieMechanist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(3,
                new CardTypePredicate(CardType.ARTIFACT)));
    }
}
