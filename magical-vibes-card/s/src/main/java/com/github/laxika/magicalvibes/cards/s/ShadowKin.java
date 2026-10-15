package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect;

@CardRegistration(set = "MIC", collectorNumber = "16")
@CardRegistration(set = "MIC", collectorNumber = "54")
public class ShadowKin extends Card {

    public ShadowKin() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect());
    }
}
