package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ArtifactOrCreatureEnteringDontCauseTriggersEffect;

@CardRegistration(set = "MKM", collectorNumber = "13")
public class DoorkeeperThrull extends Card {

    public DoorkeeperThrull() {
        addEffect(EffectSlot.STATIC, new ArtifactOrCreatureEnteringDontCauseTriggersEffect());
    }
}
