package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "82")
public class Undertow extends Card {

    public Undertow() {
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect(Keyword.ISLANDWALK));
    }
}
