package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;

@CardRegistration(set = "GRN", collectorNumber = "155")
public class BeamsplitterMage extends Card {

    public BeamsplitterMage() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopySpellForEachOtherControlledCreatureEffect(true));
    }
}
