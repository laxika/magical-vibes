package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "RAV", collectorNumber = "95")
public class MoonlightBargain extends Card {

    public MoonlightBargain() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayChooseAnyNumberToHandRestToGraveyardPayLife(5, 2));
    }
}
