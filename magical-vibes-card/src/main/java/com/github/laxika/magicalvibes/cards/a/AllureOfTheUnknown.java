package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllureOfTheUnknownEffect;

@CardRegistration(set = "THB", collectorNumber = "207")
public class AllureOfTheUnknown extends Card {

    public AllureOfTheUnknown() {
        addEffect(EffectSlot.SPELL, new AllureOfTheUnknownEffect());
    }
}
