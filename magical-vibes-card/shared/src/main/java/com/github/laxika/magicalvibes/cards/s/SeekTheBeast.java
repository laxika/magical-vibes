package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;

public class SeekTheBeast extends Card {

    public SeekTheBeast() {
        addSpellEffect(new ExileTopCardsMayPlayUntilNextEndStepEffect(2));
    }
}
