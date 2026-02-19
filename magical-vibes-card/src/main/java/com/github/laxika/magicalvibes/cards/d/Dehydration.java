package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "78")
public class Dehydration extends Card {

    public Dehydration() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new EnchantedCreatureDoesntUntapEffect());
    }
}
