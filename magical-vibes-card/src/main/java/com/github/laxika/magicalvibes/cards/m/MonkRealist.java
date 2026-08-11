package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "21")
public class MonkRealist extends Card {

    public MonkRealist() {
        target(TargetFilters.enchantment()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
    }
}
