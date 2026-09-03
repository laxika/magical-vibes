package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsLoseAllAbilitiesEffect;

@CardRegistration(set = "FUT", collectorNumber = "93")
public class YixlidJailer extends Card {

    public YixlidJailer() {
        addEffect(EffectSlot.STATIC, new GraveyardCardsLoseAllAbilitiesEffect());
    }
}
