package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;

@CardRegistration(set = "MB1", collectorNumber = "106")
public class LanternOfUndersight extends Card {

    public LanternOfUndersight() {
        addEffect(EffectSlot.STATIC, new DrawFromBottomOfLibraryEffect());
    }
}
