package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ROE", collectorNumber = "87")
public class SharedDiscovery extends Card {

    public SharedDiscovery() {
        addEffect(EffectSlot.SPELL, new TapMultiplePermanentsCost(4, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
