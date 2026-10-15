package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "STX", collectorNumber = "120")
public class BasicConjuration extends Card {

    public BasicConjuration() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
