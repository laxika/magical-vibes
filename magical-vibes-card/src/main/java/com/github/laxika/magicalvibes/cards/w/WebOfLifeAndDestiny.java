package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SPM", collectorNumber = "122")
public class WebOfLifeAndDestiny extends Card {

    public WebOfLifeAndDestiny() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.CREATURE)));
    }
}
