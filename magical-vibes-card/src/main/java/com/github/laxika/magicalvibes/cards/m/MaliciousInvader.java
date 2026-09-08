package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class MaliciousInvader extends Card {

    public MaliciousInvader() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
