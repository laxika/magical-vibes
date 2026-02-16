package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;

public class Dehydration extends Card {

    public Dehydration() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new EnchantedCreatureDoesntUntapEffect());
    }
}
