package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessCollectsEvidenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "153")
public class AxebaneFerox extends Card {

    public AxebaneFerox() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessCollectsEvidenceEffect(4));
    }
}
