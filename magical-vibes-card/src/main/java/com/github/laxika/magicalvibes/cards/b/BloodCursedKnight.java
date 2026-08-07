package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "211")
public class BloodCursedKnight extends Card {

    public BloodCursedKnight() {
        // As long as you control an enchantment, Blood-Cursed Knight gets +1/+1 and has lifelink.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentIsEnchantmentPredicate()), new StaticBoostEffect(1, 1, Set.of(Keyword.LIFELINK), GrantScope.SELF)));
    }
}
