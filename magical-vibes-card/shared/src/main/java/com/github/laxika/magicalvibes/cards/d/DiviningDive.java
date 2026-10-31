package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SeekChosenLandOrNonlandEffect;

public class DiviningDive extends Card {

    public DiviningDive() {
        addSpellEffect(new SeekChosenLandOrNonlandEffect(1));
    }
}
