package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.Set;

public class EpharaEverSheltering extends Card {

    public EpharaEverSheltering() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsOtherPermanentCount(3, new PermanentIsEnchantmentPredicate()),
                new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.INDESTRUCTIBLE), GrantScope.SELF)
        ));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, new DrawCardEffect(1));
    }
}
