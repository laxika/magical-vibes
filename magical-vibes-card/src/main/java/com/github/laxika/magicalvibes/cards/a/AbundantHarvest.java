package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenLandOrNonlandToHandEffect;

@CardRegistration(set = "STA", collectorNumber = "48")
@CardRegistration(set = "CMM", collectorNumber = "269")
public class AbundantHarvest extends Card {

    public AbundantHarvest() {
        addEffect(EffectSlot.SPELL, new RevealUntilChosenLandOrNonlandToHandEffect());
    }
}
