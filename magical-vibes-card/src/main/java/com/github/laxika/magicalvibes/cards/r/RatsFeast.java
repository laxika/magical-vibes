package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "JUD", collectorNumber = "71")
public class RatsFeast extends Card {

    public RatsFeast() {
        addEffect(EffectSlot.SPELL, new ExileCardsFromGraveyardEffect(0, 0, true, true));
    }
}
