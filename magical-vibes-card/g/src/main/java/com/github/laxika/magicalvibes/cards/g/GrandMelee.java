package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllCreaturesMustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ONS", collectorNumber = "211")
public class GrandMelee extends Card {

    public GrandMelee() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.STATIC, new AllCreaturesMustBlockEachCombatEffect());
    }
}
