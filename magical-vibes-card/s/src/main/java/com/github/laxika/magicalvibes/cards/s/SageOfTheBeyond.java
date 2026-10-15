package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.ReduceNonHandSpellCastCostEffect;

@CardRegistration(set = "OTC", collectorNumber = "111")
@CardRegistration(set = "KHC", collectorNumber = "6")
public class SageOfTheBeyond extends Card {

    public SageOfTheBeyond() {
        addEffect(EffectSlot.STATIC, new ReduceNonHandSpellCastCostEffect(2));
        addCastingOption(new ForetellCast("{4}{U}"));
    }
}
