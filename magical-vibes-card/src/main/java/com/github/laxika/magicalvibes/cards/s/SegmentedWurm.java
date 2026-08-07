package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "TMP", collectorNumber = "269")
public class SegmentedWurm extends Card {

    public SegmentedWurm() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new PutCountersOnSourceEffect(-1, -1, 1));
    }
}
