package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "C14", collectorNumber = "46")
public class SiegeBehemoth extends Card {

    public SiegeBehemoth() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttacking(),
                new GrantEffectEffect(
                        new AssignCombatDamageAsThoughUnblockedEffect(),
                        GrantScope.ALL_OWN_CREATURES)));
    }
}
