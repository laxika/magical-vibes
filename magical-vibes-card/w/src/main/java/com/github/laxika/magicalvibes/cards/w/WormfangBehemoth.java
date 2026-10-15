package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileControllerHandWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;

@CardRegistration(set = "JUD", collectorNumber = "55")
public class WormfangBehemoth extends Card {

    public WormfangBehemoth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileControllerHandWithSourceEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
