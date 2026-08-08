package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "118")
public class WoodlotCrawler extends Card {

    public WoodlotCrawler() {
        // Forestwalk is loaded from Scryfall metadata; only protection needs engine logic.
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
    }
}
