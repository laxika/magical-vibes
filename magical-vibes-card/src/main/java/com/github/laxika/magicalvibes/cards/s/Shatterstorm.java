package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllArtifactsEffect;

@CardRegistration(set = "10E", collectorNumber = "229")
public class Shatterstorm extends Card {

    public Shatterstorm() {
        addEffect(EffectSlot.SPELL, new DestroyAllArtifactsEffect(true));
    }
}
