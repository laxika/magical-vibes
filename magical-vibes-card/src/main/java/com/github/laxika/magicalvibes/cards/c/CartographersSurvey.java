package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "190")
public class CartographersSurvey extends Card {

    public CartographersSurvey() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayPutUpToMatchingOntoBattlefieldTappedRestOnBottomRandom(
                        7, new CardTypePredicate(CardType.LAND), 2, false));
    }
}
