package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "46")
@CardRegistration(set = "TPR", collectorNumber = "32")
@CardRegistration(set = "TSB", collectorNumber = "14")
public class SoltariPriest extends Card {

    public SoltariPriest() {
        // Protection from red (shadow is auto-loaded from Scryfall).
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED)));
    }
}
