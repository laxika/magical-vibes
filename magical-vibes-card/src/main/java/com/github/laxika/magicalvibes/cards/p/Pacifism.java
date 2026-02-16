package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;

public class Pacifism extends Card {

    public Pacifism() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
    }
}
