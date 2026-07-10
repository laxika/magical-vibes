package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "247")
public class DoranTheSiegeTower extends Card {

    public DoranTheSiegeTower() {
        // Each creature assigns combat damage equal to its toughness rather than its power.
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_CREATURES));
    }
}
