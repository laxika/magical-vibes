package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;

@CardRegistration(set = "SOM", collectorNumber = "118")
public class EngulfingSlagwurm extends Card {

    public EngulfingSlagwurm() {
        addEffect(EffectSlot.ON_BLOCK, new DestroyTargetCreatureAndGainLifeEqualToToughnessEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyTargetCreatureAndGainLifeEqualToToughnessEffect());
    }
}
