package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;

public class RuneTailsEssence extends Card {

    public RuneTailsEssence() {
        addStaticEffect(new PreventAllDamageToCreaturesYouControlEffect(null));
    }
}
