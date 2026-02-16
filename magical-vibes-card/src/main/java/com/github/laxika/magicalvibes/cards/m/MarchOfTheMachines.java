package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;

public class MarchOfTheMachines extends Card {

    public MarchOfTheMachines() {
        addEffect(EffectSlot.STATIC, new AnimateNoncreatureArtifactsEffect());
    }
}
