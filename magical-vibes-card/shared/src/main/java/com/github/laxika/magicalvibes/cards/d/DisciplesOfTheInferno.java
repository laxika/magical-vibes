package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageFromNoncreatureSourcesEffect;

public class DisciplesOfTheInferno extends Card {

    public DisciplesOfTheInferno() {
        addStaticEffect(new AdditionalDamageFromNoncreatureSourcesEffect(2));
    }
}
