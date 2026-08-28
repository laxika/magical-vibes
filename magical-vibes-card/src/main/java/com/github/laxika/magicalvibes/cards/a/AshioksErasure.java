package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

@CardRegistration(set = "THB", collectorNumber = "43")
public class AshioksErasure extends Card {

    public AshioksErasure() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTruePredicate(), "Target must be a spell."))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetSpellUntilSourceLeavesEffect())
                .addEffect(EffectSlot.STATIC, new CantCastSpellsWithSameNameAsExiledCardEffect(true))
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
