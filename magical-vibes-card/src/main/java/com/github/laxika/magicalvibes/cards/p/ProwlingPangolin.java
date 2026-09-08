package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect;

@CardRegistration(set = "ONS", collectorNumber = "163")
public class ProwlingPangolin extends Card {

    public ProwlingPangolin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(2));
    }
}
