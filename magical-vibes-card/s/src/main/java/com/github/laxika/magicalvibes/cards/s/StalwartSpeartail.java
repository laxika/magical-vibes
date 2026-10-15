package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YLCI", collectorNumber = "29")
public class StalwartSpeartail extends Card {

    public StalwartSpeartail() {
        // Enrage — Whenever this creature is dealt damage, other Dinosaurs you control and Dinosaur
        // cards in your hand and library perpetually get +1/+1.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.DINOSAUR), 1, 1));

        // Whenever this creature attacks, Stalwart Speartail deals 1 damage to each creature and each
        // planeswalker.
        addEffect(EffectSlot.ON_ATTACK, new MassDamageEffect(1, false, false, true, null));
    }
}
