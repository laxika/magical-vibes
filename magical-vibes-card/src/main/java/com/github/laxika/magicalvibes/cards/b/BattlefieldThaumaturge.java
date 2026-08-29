package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "31")
public class BattlefieldThaumaturge extends Card {

    public BattlefieldThaumaturge() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostPerTargetEffect(
                new PermanentIsCreaturePredicate(), 1));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
