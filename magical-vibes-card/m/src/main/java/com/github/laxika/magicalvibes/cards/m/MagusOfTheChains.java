package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChainsOfMephistophelesDrawReplacementEffect;

@CardRegistration(set = "MB2", collectorNumber = "313")
public class MagusOfTheChains extends Card {

    public MagusOfTheChains() {
        addEffect(EffectSlot.STATIC, new ChainsOfMephistophelesDrawReplacementEffect());
    }
}
