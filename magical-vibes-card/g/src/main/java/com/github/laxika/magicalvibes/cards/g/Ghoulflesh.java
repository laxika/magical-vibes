package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "103")
public class Ghoulflesh extends Card {

    public Ghoulflesh() {
        target(TargetFilters.creature())
        // Enchanted creature gets -1/-1
        .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE))
        // and is a black Zombie in addition to its other colors and types
        .addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLACK, GrantScope.ENCHANTED_CREATURE))
        .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.ZOMBIE, GrantScope.ENCHANTED_CREATURE));
    }
}
