package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "RNA", collectorNumber = "82")
public class PlagueWight extends Card {

    public PlagueWight() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostTargetCreatureEffect(-1, -1),
                TriggerMode.PER_BLOCKER);
    }
}
