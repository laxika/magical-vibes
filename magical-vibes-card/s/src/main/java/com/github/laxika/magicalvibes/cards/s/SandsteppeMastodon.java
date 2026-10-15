package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "FRF", collectorNumber = "137")
public class SandsteppeMastodon extends Card {

    public SandsteppeMastodon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BolsterEffect(5));
    }
}
