package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;

public class HurkylsRecall extends Card {

    public HurkylsRecall() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ReturnArtifactsTargetPlayerOwnsToHandEffect());
    }
}
