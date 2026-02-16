package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

public class RooftopPercher extends Card {

    public RooftopPercher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(2, 3));
    }
}
