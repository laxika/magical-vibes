package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseLoyaltyAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "MH2", collectorNumber = "189")
public class CarthTheLion extends Card {

    public CarthTheLion() {
        LookAtTopCardsEffect search = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                7, new CardTypePredicate(CardType.PLANESWALKER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, search);
        addEffect(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsPlaneswalkerPredicate(), search));
        addEffect(EffectSlot.STATIC,
                new IncreaseLoyaltyAbilityCostEffect(new PermanentIsPlaneswalkerPredicate(), 1));
    }
}
