package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "NEO", collectorNumber = "50")
public class DiscoverTheImpossible extends Card {

    public DiscoverTheImpossible() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToExileFaceDownRestToBottomRandomAndMayCast(5, 2));
    }
}
