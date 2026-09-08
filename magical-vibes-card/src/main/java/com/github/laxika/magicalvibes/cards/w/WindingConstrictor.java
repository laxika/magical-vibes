package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlayerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneCounterToArtifactOrCreatureEffect;

@CardRegistration(set = "AER", collectorNumber = "140")
public class WindingConstrictor extends Card {

    public WindingConstrictor() {
        addEffect(EffectSlot.STATIC, new AddOneCounterToArtifactOrCreatureEffect());
        addEffect(EffectSlot.STATIC, new AddOnePlayerCounterEffect());
    }
}
