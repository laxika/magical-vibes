package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MBS", collectorNumber = "82")
public class LeadTheStampede extends Card {

    public LeadTheStampede() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealAnyNumberToHandRestOnBottom(5, new CardTypePredicate(CardType.CREATURE)));
    }
}
