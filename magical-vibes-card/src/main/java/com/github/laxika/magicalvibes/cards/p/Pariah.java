package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;

public class Pariah extends Card {

    public Pariah() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToEnchantedCreatureEffect());
    }
}
