package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnFromGraveyardInsteadOfDrawEffect;

@CardRegistration(set = "6ED", collectorNumber = "132")
public class ForbiddenCrypt extends Card {

    public ForbiddenCrypt() {
        // "If you would draw a card, return a card from your graveyard to your hand instead.
        //  If you can't, you lose the game."
        addEffect(EffectSlot.STATIC, new ReturnFromGraveyardInsteadOfDrawEffect());
        // "If a card would be put into your graveyard from anywhere, exile that card instead."
        addEffect(EffectSlot.STATIC, new ExileOwnCardsInsteadOfGraveyardEffect());
    }
}
