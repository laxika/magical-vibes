package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect;

@CardRegistration(set = "INV", collectorNumber = "18")
public class GlobalRuin extends Card {

    public GlobalRuin() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect());
    }
}
