package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;

@CardRegistration(set = "SOM", collectorNumber = "29")
public class BondsOfQuicksilver extends Card {

    public BondsOfQuicksilver() {
        addEffect(EffectSlot.STATIC, new EnchantedCreatureDoesntUntapEffect());
    }
}
