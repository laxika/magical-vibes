package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;

public class RuneTailsEssence extends Card {

    public RuneTailsEssence() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageToCreaturesYouControlEffect(null));
    }
}
