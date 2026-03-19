package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "10E", collectorNumber = "256")
public class CommuneWithNature extends Card {

    public CommuneWithNature() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(5, new CardTypePredicate(CardType.CREATURE)));
    }
}
