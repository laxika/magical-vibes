package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachPriorInstantOrSorceryEffect;

@CardRegistration(set = "FDN", collectorNumber = "248")
public class ThousandYearStorm extends Card {

    public ThousandYearStorm() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopySpellForEachPriorInstantOrSorceryEffect());
    }
}
