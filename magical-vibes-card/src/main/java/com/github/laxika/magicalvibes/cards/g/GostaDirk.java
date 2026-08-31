package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "227")
public class GostaDirk extends Card {

    public GostaDirk() {
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect(Keyword.ISLANDWALK));
    }
}
