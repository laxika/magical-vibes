package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesGainLifePerCreatureEffect;

@CardRegistration(set = "P02", collectorNumber = "128")
public class HarmonyOfNature extends Card {

    public HarmonyOfNature() {
        addSpellEffect(new TapCreaturesGainLifePerCreatureEffect(4));
    }
}
