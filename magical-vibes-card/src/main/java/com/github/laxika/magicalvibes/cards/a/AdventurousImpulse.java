package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "153")
public class AdventurousImpulse extends Card {

    public AdventurousImpulse() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3,
                new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.CREATURE), new CardTypePredicate(CardType.LAND)))));
    }
}
