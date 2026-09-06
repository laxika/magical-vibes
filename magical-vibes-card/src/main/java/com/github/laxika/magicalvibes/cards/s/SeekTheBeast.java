package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;

public class SeekTheBeast extends Card {

    public SeekTheBeast() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextEndStepEffect(2));
    }
}
