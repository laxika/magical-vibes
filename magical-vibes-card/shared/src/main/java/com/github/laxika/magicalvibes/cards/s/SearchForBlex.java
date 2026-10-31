package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

public class SearchForBlex extends Card {

    public SearchForBlex() {
        addSpellEffect(LookAtTopCardsEffect.mayChooseAnyNumberToHandRestToGraveyardLoseLife(5, 3));
    }
}
