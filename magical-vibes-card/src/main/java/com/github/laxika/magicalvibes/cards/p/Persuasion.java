package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;

public class Persuasion extends Card {

    public Persuasion() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
