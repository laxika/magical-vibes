package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "111")
public class Mourning extends Card {

    public Mourning() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, 0, GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(ReturnToHandEffect.self()),
                "{B}: Return this Aura to its owner's hand."));
    }
}
