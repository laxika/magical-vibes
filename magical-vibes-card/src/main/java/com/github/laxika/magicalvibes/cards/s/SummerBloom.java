package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "9ED", collectorNumber = "273")
@CardRegistration(set = "POR", collectorNumber = "187")
public class SummerBloom extends Card {

    public SummerBloom() {
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(3));
    }
}
