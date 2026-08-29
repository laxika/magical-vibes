package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "21")
@CardRegistration(set = "BRB", collectorNumber = "83")
public class SunClasp extends Card {

    public SunClasp() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature gets +1/+3.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 3, GrantScope.ENCHANTED_CREATURE));
        // {W}: Return enchanted creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(ReturnToHandEffect.enchanted()),
                "{W}: Return enchanted creature to its owner's hand."));
    }
}
