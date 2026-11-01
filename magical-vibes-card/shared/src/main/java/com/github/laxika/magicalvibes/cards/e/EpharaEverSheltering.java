package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

public class EpharaEverSheltering extends Card {

    public EpharaEverSheltering() {
        addStaticEffect(new ConditionalEffect(
                new ControlsOtherPermanentCount(3, new PermanentIsEnchantmentPredicate()),
                new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.INDESTRUCTIBLE), GrantScope.SELF)
        ));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), new DrawCardEffect(1));
    }
}
