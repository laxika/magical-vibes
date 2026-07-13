package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndGainControlIfArtifactOrCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "64")
public class Desertion extends Card {

    public Desertion() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndGainControlIfArtifactOrCreatureEffect());
    }
}
