package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "15")
@CardRegistration(set = "SHM", collectorNumber = "6")
public class GoldenglowMoth extends Card {

    public GoldenglowMoth() {
        // Flying keyword is auto-loaded from Scryfall.
        // Whenever Goldenglow Moth blocks, you may gain 4 life.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new MayEffect(new GainLifeEffect(4), "Gain 4 life?"));
    }
}
