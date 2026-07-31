package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "41a")
@CardRegistration(set = "ALL", collectorNumber = "41b")
public class VisceridArmor extends Card {

    public VisceridArmor() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature gets +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
        // {1}{U}: Return this Aura to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(ReturnToHandEffect.self()), "{1}{U}: Return Viscerid Armor to its owner's hand."));
    }
}
