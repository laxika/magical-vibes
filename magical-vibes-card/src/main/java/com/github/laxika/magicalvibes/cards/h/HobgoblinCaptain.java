package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "AFR", collectorNumber = "148")
public class HobgoblinCaptain extends Card {

    public HobgoblinCaptain() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
    }
}
