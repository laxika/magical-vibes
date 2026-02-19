package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "95")
public class Persuasion extends Card {

    public Persuasion() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
