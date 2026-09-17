package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "VMA", collectorNumber = "243")
@CardRegistration(set = "PC2", collectorNumber = "82")
@CardRegistration(set = "EMA", collectorNumber = "196")
@CardRegistration(set = "PCA", collectorNumber = "82")
@CardRegistration(set = "2XM", collectorNumber = "191")
@CardRegistration(set = "C13", collectorNumber = "177")
public class BalefulStrix extends Card {

    public BalefulStrix() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }
}
