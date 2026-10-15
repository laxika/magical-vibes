package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

public class SearchForBlex extends Card {

    public SearchForBlex() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayChooseAnyNumberToHandRestToGraveyardLoseLife(5, 3));
    }
}
